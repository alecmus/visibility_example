package com.github.alecmus.visibility_example.process;

import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ZeebeVisibilityProcessImpl implements ZeebeVisibilityProcess {

    @Value("${visibility.enabled:false}")
    private boolean visibilityEnabled;

    private final ZeebeClient zeebeClient;

    @Autowired
    public ZeebeVisibilityProcessImpl(ZeebeClient zeebeClient) {
        this.zeebeClient = zeebeClient;
    }

    @Override
    public void startProcess(String processId, String correlationKey) {
        startProcess(processId, correlationKey, Map.of());
    }

    @Override
    public void startProcess(String processId, String correlationKey, Map<String, Object> variables) {
        if (visibilityEnabled) {
            // add correlationKey String as process variable
            Map<String, Object> process_variables = new HashMap<>();
            process_variables.put("correlationKey", correlationKey);

            variables.forEach((key, value) -> process_variables.merge(key, value, (v1, v2) -> v1));

            zeebeClient.newCreateInstanceCommand()
                    .bpmnProcessId(processId)
                    .latestVersion()
                    .variables(process_variables)
                    .send();
        }
    }

    @Override
    public void sendMessage(String messageName, String correlationKey) {
        sendMessage(messageName, correlationKey, Map.of());
    }

    @Override
    public void sendMessage(String messageName, String correlationKey, Map<String, Object> variables) {
        if (visibilityEnabled) {
            // send message to process
            zeebeClient.newPublishMessageCommand()
                    .messageName(messageName)
                    .correlationKey(correlationKey)
                    .variables(variables)
                    .send();
        }
    }
}
