package com.bondbenbond.oauthintegrationapi.config;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String uiBaseUrl = "https://auth-ui.bondbenbond.com";
    private String allowedOrigin = "https://auth-ui.bondbenbond.com";
    private boolean debugTokens = false;
    private Map<String, String> testResourceUris = new LinkedHashMap<>();

    public String getUiBaseUrl() {
        return uiBaseUrl;
    }

    public void setUiBaseUrl(String uiBaseUrl) {
        this.uiBaseUrl = uiBaseUrl;
    }

    public String getAllowedOrigin() {
        return allowedOrigin;
    }

    public void setAllowedOrigin(String allowedOrigin) {
        this.allowedOrigin = allowedOrigin;
    }

    public boolean isDebugTokens() {
        return debugTokens;
    }

    public void setDebugTokens(boolean debugTokens) {
        this.debugTokens = debugTokens;
    }

    public Map<String, String> getTestResourceUris() {
        return testResourceUris;
    }

    public void setTestResourceUris(Map<String, String> testResourceUris) {
        this.testResourceUris = testResourceUris;
    }
}
