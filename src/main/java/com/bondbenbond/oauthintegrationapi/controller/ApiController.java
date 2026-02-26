package com.bondbenbond.oauthintegrationapi.controller;

import com.bondbenbond.oauthintegrationapi.dto.ProviderInfoResponse;
import com.bondbenbond.oauthintegrationapi.dto.WhoAmIResponse;
import com.bondbenbond.oauthintegrationapi.service.IdentityService;
import com.bondbenbond.oauthintegrationapi.service.ProviderApiService;
import com.bondbenbond.oauthintegrationapi.service.ProviderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final IdentityService identityService;
    private final ProviderService providerService;
    private final ProviderApiService providerApiService;

    public ApiController(IdentityService identityService,
                         ProviderService providerService,
                         ProviderApiService providerApiService) {
        this.identityService = identityService;
        this.providerService = providerService;
        this.providerApiService = providerApiService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @GetMapping("/whoami")
    public WhoAmIResponse whoami(OAuth2AuthenticationToken authentication, HttpServletRequest request) {
        return identityService.getIdentity(authentication, request);
    }

    @GetMapping("/providers")
    public List<ProviderInfoResponse> providers() {
        return providerService.listProviders();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/test/call")
    public Object testCall(OAuth2AuthenticationToken authentication, HttpServletRequest request) {
        return providerApiService.callUserInfoOrConfiguredResource(authentication, request);
    }
}
