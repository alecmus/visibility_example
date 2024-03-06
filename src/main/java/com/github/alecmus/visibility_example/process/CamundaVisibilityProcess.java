package com.github.alecmus.visibility_example.process;

import java.util.Map;

public interface CamundaVisibilityProcess {

    Properties startProcess(String processId, String correlationKey);
    void sendMessage(String messageName, String correlationKey);
    void sendMessage(String messageName, String correlationKey, Map<String, Object> variables);
    void addVariables(Long instanceKey, Map<String, Object> variables);

    class Properties {
        private Long instanceKey;
        private String correlationKey;

        public String getCorrelationKey() {
            return correlationKey;
        }

        public void setCorrelationKey(String correlationKey) {
            this.correlationKey = correlationKey;
        }

        public Long getInstanceKey() {
            return instanceKey;
        }

        public void setInstanceKey(Long instanceKey) {
            this.instanceKey = instanceKey;
        }
    }
}
