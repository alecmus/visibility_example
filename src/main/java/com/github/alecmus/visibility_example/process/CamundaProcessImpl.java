package com.github.alecmus.visibility_example.process;

import io.camunda.zeebe.client.ZeebeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Component
public class CamundaProcessImpl implements CamundaProcess {

    private final Logger log = LoggerFactory.getLogger(CamundaProcessImpl.class);
    private final ZeebeClient zeebeClient;

    @Autowired
    public CamundaProcessImpl(ZeebeClient zeebeClient) {
        this.zeebeClient = zeebeClient;
    }


    @Override
    public Properties startProcess(String processId) {
        log.debug("Starting process: '" + processId + "' ...");
        Properties properties = new Properties();

        try {
            // create Zeebe process instance and add instanceUUID as a process variable
            // for use as a message correlation key
            final String instanceUUID = UUID.randomUUID().toString();
            Map<String, Object> process_variables = Map.of("instanceUUID", instanceUUID);

            long instanceKey = zeebeClient.newCreateInstanceCommand()
                    .bpmnProcessId(processId)
                    .latestVersion()
                    .variables(process_variables)
                    .send().join()
                    .getProcessInstanceKey();

            properties.setInstanceKey(instanceKey);
            properties.setCorrelationKey(instanceUUID);

            log.debug("Process '" + processId + "' started");
        } catch (Exception e) {
            log.debug("Failed to start process '" + processId + "': " + e.getMessage());
        }

        return properties;
    }

    @Override
    public void sendMessage(String messageName, String correlationKey) {
        try {
            log.debug("Sending message '" + messageName + "' - correlationKey = " + correlationKey);

            // send message to process
            zeebeClient.newPublishMessageCommand()
                    .messageName(messageName)
                    .correlationKey(correlationKey)
                    .timeToLive(Duration.ofSeconds(1))
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
