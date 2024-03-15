package com.github.alecmus.visibility_example.config;

import io.camunda.zeebe.client.ZeebeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ZeebeProcessDeployer implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(BeanPostProcessor.class);

    private Environment env;

    @Autowired
    public ZeebeProcessDeployer(Environment env) {
        this.env = env;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {

        if (bean instanceof ZeebeClient) {

            ZeebeClient client = (ZeebeClient)bean;

            log.info("Deploying Zeebe processes");

            List<String> files = Arrays.asList(env.getProperty("visibility.files").split(","));

            for (String file : files) {
                client.newDeployResourceCommand()
                        .addResourceFromClasspath(file)
                        .send()
                        .join();
            }

            log.info("Zeebe processes deployed from: " + files);
        }

        return bean;
    }
}
