package com.bondbenbond.oauthintegrationapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WhoAmIResponse(
    String provider,
    String subject,
    String email,
    String name,
    Set<String> scopes,
    Instant expiresAt,
    Map<String, Object> claims,
    String accessToken,
    String refreshToken
) {
}
