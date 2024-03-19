package com.github.alecmus.visibility_example.process;

import java.util.Map;

public interface ZeebeVisibilityProcess {

    void startProcess(String processId, String correlationKey);
    void startProcess(String processId, String correlationKey, Map<String, Object> variables);
    void sendMessage(String messageName, String correlationKey);
    void sendMessage(String messageName, String correlationKey, Map<String, Object> variables);
}
