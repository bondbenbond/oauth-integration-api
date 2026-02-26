package com.bondbenbond.oauthintegrationapi;

import com.bondbenbond.oauthintegrationapi.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class OauthIntegrationApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(OauthIntegrationApiApplication.class, args);
    }
}
