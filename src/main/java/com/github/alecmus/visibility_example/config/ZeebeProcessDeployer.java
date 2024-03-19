package com.github.alecmus.visibility_example.config;

import io.camunda.zeebe.client.ZeebeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ZeebeProcessDeployer implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(BeanPostProcessor.class);

    @Value("${visibility.enabled:false}")
    private boolean visibilityEnabled;

    @Value("${visibility.files:}")
    private List<String> visibilityFiles;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {

        if (bean instanceof ZeebeClient) {

            if (visibilityEnabled) {
                ZeebeClient client = (ZeebeClient) bean;

                log.info("Deploying Zeebe processes");

                for (String file : visibilityFiles) {
                    client.newDeployResourceCommand()
                            .addResourceFromClasspath(file)
                            .send()
                            .join();
                }

                log.info("Zeebe processes deployed from: " + visibilityFiles);
            } else {
                log.info("Visibility disabled, no attempt made to deploy Zeebe processes.");
            }
        }

        return bean;
    }
}
