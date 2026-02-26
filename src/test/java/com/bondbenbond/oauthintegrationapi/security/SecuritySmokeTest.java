package com.bondbenbond.oauthintegrationapi.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecuritySmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthIsPublicWhoamiIsProtected() throws Exception {
        mockMvc.perform(get("/api/health")).andExpect(status().isOk());
        mockMvc.perform(get("/api/whoami")).andExpect(status().isUnauthorized());
    }

    @Test
    void stateChangingEndpointsRequireCsrf() throws Exception {
        mockMvc.perform(post("/api/test/call").with(oauth2Login()))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/test/call").with(oauth2Login()).with(csrf()))
            .andExpect(status().isBadRequest());
    }
}
