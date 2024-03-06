package com.github.alecmus.visibility_example.process;

import io.camunda.zeebe.client.ZeebeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@Component
public class CamundaProcessImpl implements CamundaProcess {

    private final Logger log = LoggerFactory.getLogger(CamundaProcessImpl.class);
    private final ZeebeClient zeebeClient;

    @Autowired
    public CamundaProcessImpl(ZeebeClient zeebeClient) {
        this.zeebeClient = zeebeClient;
    }

    @Override
    public Properties startProcess(String processId, String correlationKey) {
        log.debug("Starting process: '" + processId + "' ...");
        Properties properties = new Properties();

        try {
            // add correlationKey String as process variable
            Map<String, Object> process_variables = Map.of("correlationKey", correlationKey);

             long instanceKey = zeebeClient.newCreateInstanceCommand()
                     .bpmnProcessId(processId)
                     .latestVersion()
                     .variables(process_variables)
                     .send().join()
                     .getProcessInstanceKey();

            properties.setInstanceKey(instanceKey);
            properties.setCorrelationKey(correlationKey);

            log.debug("Process '" + processId + "' started");
        } catch (Exception e) {
            log.debug("Failed to start process '" + processId + "': " + e.getMessage());
        }

        return properties;
    }

    @Override
    public void sendMessage(String messageName, String correlationKey) {
        sendMessage(messageName, correlationKey, Map.of());
    }

    @Override
    public void sendMessage(String messageName, String correlationKey, Map<String, Object> variables) {
        try {
            log.debug("Sending message '" + messageName + "' - correlationKey = " + correlationKey);

            // send message to process
            zeebeClient.newPublishMessageCommand()
                    .messageName(messageName)
                    .correlationKey(correlationKey)
                    .variables(variables)
                    .timeToLive(Duration.ofSeconds(1))
                    .requestTimeout(Duration.ofSeconds(1))
                    .send().join();

            log.debug("Message '" + messageName + "' sent");
        } catch (Exception e) {
            log.debug("Failed to send message '" + messageName + ": " + e.getMessage());
        }
    }

    @Override
    public void addVariables(Long instanceKey, Map<String, Object> variables) {
        try {
            log.debug("Adding variables to process: " + variables.keySet());

            zeebeClient.newSetVariablesCommand(instanceKey)
                    .variables(variables)
                    .send().join();
        } catch (Exception e) {
            log.debug("Failed to add variables to process instance '" + instanceKey + "': " + e.getMessage());
        }
    }
}
