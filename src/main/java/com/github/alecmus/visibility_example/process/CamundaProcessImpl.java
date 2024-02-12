package com.github.alecmus.visibility_example.process;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
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

    @Override
    public void completeServiceTask(String jobType) {
        try {
            log.debug("Completing job '" + jobType + "'");

            // activate the job
            final List<ActivatedJob> activatedJobs = zeebeClient.newActivateJobsCommand()
                    .jobType(jobType)
                    .maxJobsToActivate(1)
                    .send().join()
                    .getJobs();

            if (activatedJobs.size() != 1)
                throw new IllegalStateException("Job not found");

            for (ActivatedJob job : activatedJobs) {
                // complete the job
                zeebeClient.newCompleteCommand(job)
                        .send().join();
            }
        } catch (Exception e) {
            log.debug("Error completing '" + jobType + "': " + e.getMessage());
        }
    }

    public void failServiceTask(String jobType, String errorCode) {
        try {
            log.debug("Failing job '" + jobType + "'");

            // activate the job
            final List<ActivatedJob> activatedJobs = zeebeClient.newActivateJobsCommand()
                    .jobType(jobType)
                    .maxJobsToActivate(1)
                    .send().join()
                    .getJobs();

            if (activatedJobs.size() != 1)
                throw new IllegalStateException("Job not found");

            for (ActivatedJob job : activatedJobs) {
                // fail the job. This will be handled by an error catch event
                // identified by the given errorCode.
                zeebeClient.newThrowErrorCommand(job.getKey())
                        .errorCode(errorCode)
                        .send().join();
            }
        } catch (Exception e) {
            log.debug("Error failing job '" + jobType + "': " + e.getMessage());
        }
    }
}
