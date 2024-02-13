package com.github.alecmus.visibility_example.config;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZeebeConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ZeebeConfiguration.class);

    //Zeebe Client Credentials
    @Value("${zeebe.client.cloud.region}")
    private String zeebeRegion;

    @Value("${zeebe.client.cloud.clusterId}")
    private String zeebeClusterId;

    @Value("${zeebe.client.cloud.clientId}")
    private String zeebeClientId = "rkP~HxfGAk3nc.Z5ETNLOFk31vN1Opew";

    @Value("${zeebe.client.cloud.clientSecret}")
    private String zeebeClientSecret;

    // This bean will only be created if Spring Boot autoconfiguration doesn't automatically
    // create it, e.g., on Spring Boot 2.5
    @Bean
    @ConditionalOnMissingBean(ZeebeClient.class)
    public ZeebeClient zeebeClient() {

        // create Zeebe client bean
        log.info("Zeebe client bean not found, creating one ...");

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

    // This will be executed after the ApplicationContext has been
    // created but just before the Spring Boot application starts up
    @Bean
    public CommandLineRunner processDeployment(ZeebeClient client) {
        return applicationArguments -> {

            final String processFile = "visibility-process.bpmn";

            log.info("Deploying process from file " + processFile);

            client.newDeployResourceCommand()
                    .addResourceFromClasspath(processFile)
                    .send()
                    .join();

            log.info("Process deployed from file " + processFile);
        };
    }
}
