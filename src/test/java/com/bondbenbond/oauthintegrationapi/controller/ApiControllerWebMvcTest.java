package com.bondbenbond.oauthintegrationapi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bondbenbond.oauthintegrationapi.dto.ProviderInfoResponse;
import com.bondbenbond.oauthintegrationapi.dto.WhoAmIResponse;
import com.bondbenbond.oauthintegrationapi.config.AppProperties;
import com.bondbenbond.oauthintegrationapi.config.SecurityConfig;
import com.bondbenbond.oauthintegrationapi.service.IdentityService;
import com.bondbenbond.oauthintegrationapi.service.ProviderApiService;
import com.bondbenbond.oauthintegrationapi.service.ProviderService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ApiController.class)
@Import({SecurityConfig.class, ApiControllerWebMvcTest.TestConfig.class})
class ApiControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IdentityService identityService;

    @MockBean
    private ProviderService providerService;

    @MockBean
    private ProviderApiService providerApiService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        AppProperties appProperties() {
            AppProperties properties = new AppProperties();
            properties.setAllowedOrigin("https://auth-ui.bondbenbond.com");
            properties.setUiBaseUrl("https://auth-ui.bondbenbond.com");
            return properties;
        }
    }

    @Test
    void healthShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void providersShouldBePublic() throws Exception {
        when(providerService.listProviders()).thenReturn(List.of(
            new ProviderInfoResponse("okta", "Okta", "https://example/authorize", "https://example/userinfo", true, true,
                Map.of("oauth2Login", true, "userinfo", true, "configuredResource", false))
        ));

        mockMvc.perform(get("/api/providers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].registrationId").value("okta"));
    }

    @Test
    void whoamiShouldReturnIdentityWhenAuthenticated() throws Exception {
        when(identityService.getIdentity(any(), any())).thenReturn(
            new WhoAmIResponse("okta", "sub123", "dev@example.com", "Dev User", Set.of("openid"), Instant.now(), null, null, null)
        );

        mockMvc.perform(get("/api/whoami").with(oauth2Login()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.provider").value("okta"))
            .andExpect(jsonPath("$.subject").value("sub123"));
    }

    @Test
    void logoutRequiresCsrf() throws Exception {
        mockMvc.perform(post("/api/logout").with(oauth2Login()))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/logout").with(oauth2Login()).with(csrf()))
            .andExpect(status().isNoContent());
    }
}
