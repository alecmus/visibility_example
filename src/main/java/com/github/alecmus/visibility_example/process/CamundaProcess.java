package com.github.alecmus.visibility_example.process;

import java.util.Map;

public interface CamundaProcess {

    Properties startProcess(String processId);
    void sendMessage(String messageName, String correlationKey);
    void addVariables(Long instanceKey, Map<String, Object> variables);
    void completeServiceTask(String jobType);
    void failServiceTask(String jobType, String errorCode);

    static class Properties {
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
