package com.bondbenbond.oauthintegrationapi.service;

import com.bondbenbond.oauthintegrationapi.config.AppProperties;
import com.bondbenbond.oauthintegrationapi.dto.WhoAmIResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class IdentityService {

    private final OAuth2AuthorizedClientRepository authorizedClientRepository;
    private final AppProperties appProperties;
    private final Environment environment;

    public IdentityService(OAuth2AuthorizedClientRepository authorizedClientRepository,
                           AppProperties appProperties,
                           Environment environment) {
        this.authorizedClientRepository = authorizedClientRepository;
        this.appProperties = appProperties;
        this.environment = environment;
    }

    public WhoAmIResponse getIdentity(OAuth2AuthenticationToken authentication, HttpServletRequest request) {
        OAuth2User principal = authentication.getPrincipal();
        OAuth2AuthorizedClient authorizedClient = authorizedClientRepository.loadAuthorizedClient(
            authentication.getAuthorizedClientRegistrationId(),
            authentication,
            request
        );

        String subject = principal.getName();
        String email = attributeAsString(principal, "email");
        String name = attributeAsString(principal, "name");
        if (principal instanceof OidcUser oidcUser) {
            subject = oidcUser.getSubject();
            email = Optional.ofNullable(oidcUser.getEmail()).orElse(email);
            name = Optional.ofNullable(oidcUser.getFullName()).orElse(name);
        }

        Set<String> scopes = Set.of();
        java.time.Instant expiresAt = null;
        String accessToken = null;
        String refreshToken = null;

        if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
            scopes = authorizedClient.getAccessToken().getScopes();
            expiresAt = authorizedClient.getAccessToken().getExpiresAt();

            if (isLocalDebugEnabled()) {
                accessToken = authorizedClient.getAccessToken().getTokenValue();
                if (authorizedClient.getRefreshToken() != null) {
                    refreshToken = authorizedClient.getRefreshToken().getTokenValue();
                }
            }
        }

        Map<String, Object> claims = isLocalDebugEnabled() ? principal.getAttributes() : null;

        return new WhoAmIResponse(
            authentication.getAuthorizedClientRegistrationId(),
            subject,
            email,
            name,
            scopes,
            expiresAt,
            claims,
            accessToken,
            refreshToken
        );
    }

    private boolean isLocalDebugEnabled() {
        return appProperties.isDebugTokens() && environment.matchesProfiles("local");
    }

    private static String attributeAsString(OAuth2User user, String key) {
        Object value = user.getAttribute(key);
        return value == null ? null : value.toString();
    }
}
