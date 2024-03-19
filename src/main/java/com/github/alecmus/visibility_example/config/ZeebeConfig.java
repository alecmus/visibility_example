package com.github.alecmus.visibility_example.config;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ZeebeConfig {

    private static final Logger log = LoggerFactory.getLogger(ZeebeConfig.class);

    @Autowired
    private Environment env;

    @Bean
    @ConditionalOnMissingBean(ZeebeClient.class)
    public ZeebeClient zeebeClient() {

        // create Zeebe client bean
        log.info("Zeebe client bean not found, creating one ...");

        final String zeebeRegion = env.getProperty("zeebe.client.cloud.region", "fakeRegion");
        final String zeebeClusterId = env.getProperty("zeebe.client.cloud.clusterId", "fakeClusterId");
        final String zeebeClientId = env.getProperty("zeebe.client.cloud.clientId", "fakeClientId");
        final String zeebeClientSecret = env.getProperty("zeebe.client.cloud.clientSecret", "fakeClientSecret");

        final String zeebeAddress = zeebeClusterId + "." + zeebeRegion + ".zeebe.camunda.io:443";
        final String zeebeAuthorizationServerUrl = "https://login.cloud.camunda.io/oauth/token";
        final String zeebeTokenAudience = "zeebe.camunda.io";

        final OAuthCredentialsProvider credentialsProvider =
                new OAuthCredentialsProviderBuilder()
                        .authorizationServerUrl(zeebeAuthorizationServerUrl)
                        .audience(zeebeTokenAudience)
                        .clientId(zeebeClientId)
                        .clientSecret(zeebeClientSecret)
                        .build();

        ZeebeClient client = ZeebeClient.newClientBuilder()
                .gatewayAddress(zeebeAddress)
                .credentialsProvider(credentialsProvider)
                .build();

        if (client == null)
            throw new IllegalStateException("Failed to create Zeebe client bean");

        log.info("Zeebe client bean created successfully");
        return client;
    }
}
