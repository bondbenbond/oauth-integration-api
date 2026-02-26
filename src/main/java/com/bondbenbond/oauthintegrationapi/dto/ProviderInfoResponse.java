package com.bondbenbond.oauthintegrationapi.dto;

import java.util.Map;

public record ProviderInfoResponse(
    String registrationId,
    String displayName,
    String authorizationUri,
    String userInfoUri,
    boolean supportsUserInfo,
    boolean supportsTestCall,
    Map<String, Boolean> capabilities
) {
}
