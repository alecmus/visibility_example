package com.github.alecmus.visibility_example.process;

import java.util.Map;

public interface CamundaProcess {

    Properties startProcess(String processId);
    void sendMessage(String messageName, String correlationKey);
    void addVariables(Long instanceKey, Map<String, Object> variables);
    void completeServiceTask(String jobType);
    void completeServiceTask(String jobType, Map<String, Object> variables, boolean processVariables);
    void failServiceTask(String jobType, String errorCode);
    void failServiceTask(String jobType, String errorCode, Map<String, Object> variables, boolean processVariables);

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
