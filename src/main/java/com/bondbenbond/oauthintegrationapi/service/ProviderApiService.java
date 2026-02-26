package com.bondbenbond.oauthintegrationapi.service;

import com.bondbenbond.oauthintegrationapi.config.AppProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProviderApiService {

    private final OAuth2AuthorizedClientRepository authorizedClientRepository;
    private final InMemoryClientRegistrationRepository clientRegistrationRepository;
    private final AppProperties appProperties;
    private final RestClient restClient;

    public ProviderApiService(ObjectProvider<OAuth2AuthorizedClientRepository> authorizedClientRepositoryProvider,
                              ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider,
                              AppProperties appProperties,
                              RestClient.Builder restClientBuilder) {
        this.authorizedClientRepository = authorizedClientRepositoryProvider.getIfAvailable();
        ClientRegistrationRepository clientRegistrationRepository = clientRegistrationRepositoryProvider.getIfAvailable();
        this.clientRegistrationRepository =
            clientRegistrationRepository instanceof InMemoryClientRegistrationRepository repo ? repo : null;
        this.appProperties = appProperties;
        this.restClient = restClientBuilder.build();
    }

    public Object callUserInfoOrConfiguredResource(OAuth2AuthenticationToken authentication, HttpServletRequest request) {
        if (authorizedClientRepository == null) {
            throw new ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "OAuth2 client support is not configured"
            );
        }
        String registrationId = authentication.getAuthorizedClientRegistrationId();
        OAuth2AuthorizedClient authorizedClient = authorizedClientRepository.loadAuthorizedClient(
            registrationId,
            authentication,
            request
        );
        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            throw new ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "No authorized client available for current session"
            );
        }

        String targetUri = resolveTargetUri(registrationId);
        if (targetUri == null || targetUri.isBlank()) {
            throw new ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "No userinfo endpoint or test resource configured for provider: " + registrationId
            );
        }

        try {
            return restClient.get()
                .uri(targetUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authorizedClient.getAccessToken().getTokenValue())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new ResponseStatusException(
                        res.getStatusCode(),
                        "Provider call failed with status: " + res.getStatusCode().value()
                    );
                })
                .body(Object.class);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_GATEWAY,
                "Provider call failed",
                ex
            );
        }
    }

    private String resolveTargetUri(String registrationId) {
        String configured = appProperties.getTestResourceUris().get(registrationId);
        if (configured != null && !configured.isBlank()) {
            return configured;
        }

        if (clientRegistrationRepository == null) {
            return null;
        }

        if (clientRegistrationRepository.findByRegistrationId(registrationId) == null) {
            return null;
        }

        return clientRegistrationRepository.findByRegistrationId(registrationId)
            .getProviderDetails()
            .getUserInfoEndpoint()
            .getUri();
    }
}
