package com.github.alecmus.visibility_example.process;

import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CamundaVisibilityProcessImpl implements CamundaVisibilityProcess {

    private final ZeebeClient zeebeClient;

    @Autowired
    public CamundaVisibilityProcessImpl(ZeebeClient zeebeClient) {
        this.zeebeClient = zeebeClient;
    }

    @Override
    public void startProcess(String processId, String correlationKey) {
        // add correlationKey String as process variable
        Map<String, Object> process_variables = Map.of("correlationKey", correlationKey);

        zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId(processId)
                .latestVersion()
                .variables(process_variables)
                .send();
    }

    @Override
    public void sendMessage(String messageName, String correlationKey) {
        sendMessage(messageName, correlationKey, Map.of());
    }

    @Override
    public void sendMessage(String messageName, String correlationKey, Map<String, Object> variables) {
        // send message to process
        zeebeClient.newPublishMessageCommand()
                .messageName(messageName)
                .correlationKey(correlationKey)
                .variables(variables)
                .send();
    }
}
