package com.bondbenbond.oauthintegrationapi.service;

import com.bondbenbond.oauthintegrationapi.config.AppProperties;
import com.bondbenbond.oauthintegrationapi.dto.ProviderInfoResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.stereotype.Service;

@Service
public class ProviderService {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final AppProperties appProperties;

    public ProviderService(ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider,
                           AppProperties appProperties) {
        this.clientRegistrationRepository = clientRegistrationRepositoryProvider.getIfAvailable();
        this.appProperties = appProperties;
    }

    public List<ProviderInfoResponse> listProviders() {
        if (clientRegistrationRepository == null) {
            return List.of();
        }
        if (!(clientRegistrationRepository instanceof InMemoryClientRegistrationRepository repo)) {
            return List.of();
        }

        List<ProviderInfoResponse> providers = new ArrayList<>();
        for (ClientRegistration registration : repo) {
            String registrationId = registration.getRegistrationId();
            String userInfoUri = registration.getProviderDetails().getUserInfoEndpoint().getUri();
            boolean supportsUserInfo = userInfoUri != null && !userInfoUri.isBlank();
            boolean supportsConfiguredResource = appProperties.getTestResourceUris().containsKey(registrationId);

            Map<String, Boolean> capabilities = new LinkedHashMap<>();
            capabilities.put("oauth2Login", true);
            capabilities.put("userinfo", supportsUserInfo);
            capabilities.put("configuredResource", supportsConfiguredResource);

            providers.add(new ProviderInfoResponse(
                registrationId,
                registration.getClientName(),
                registration.getProviderDetails().getAuthorizationUri(),
                userInfoUri,
                supportsUserInfo,
                supportsUserInfo || supportsConfiguredResource,
                capabilities
            ));
        }
        return providers;
    }
}
