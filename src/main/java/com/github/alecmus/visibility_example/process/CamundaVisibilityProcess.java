package com.github.alecmus.visibility_example.process;

import java.util.Map;

public interface CamundaVisibilityProcess {

    void startProcess(String processId, String correlationKey);
    void sendMessage(String messageName, String correlationKey);
    void sendMessage(String messageName, String correlationKey, Map<String, Object> variables);
}
