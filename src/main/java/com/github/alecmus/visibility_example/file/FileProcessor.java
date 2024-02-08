package com.github.alecmus.visibility_example.file;

import io.camunda.zeebe.client.ZeebeClient;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/*
 * File Processor bean that implements the Processor functional interface.
 * This allows it to be used in the .process() chained method in the router's
 * configure() method.
 */
@Component
public class FileProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(FileProcessor.class);

    private final ZeebeClient zeebeClient;

    @Autowired
    public FileProcessor(ZeebeClient zeebeClient) {
        this.zeebeClient = zeebeClient;
    }

    @Override
    public void process(Exchange exchange) {

        try {
            // create Zeebe process instance and add instanceUUID as a process variable
            // for use as a message correlation key
            final String instanceUUID = UUID.randomUUID().toString();
            Map<String, Object> variables = Map.of("instanceUUID", instanceUUID);

            long instanceKey = zeebeClient.newCreateInstanceCommand()
                    .bpmnProcessId("Process_VisibilityProcess")
                    .latestVersion()
                    .variables(variables)
                    .send().join()
                    .getProcessInstanceKey();

            // send file received message to process
            zeebeClient.newPublishMessageCommand()
                    .messageName("Message_FileReceived")
                    .correlationKey(instanceUUID)
                    .timeToLive(Duration.ofSeconds(1))
                    .send().join();

            // add instanceUUID and instanceKey as exchange headers
            exchange.getIn().setHeader("instanceUUID", instanceUUID);
            exchange.getIn().setHeader("instanceKey", Long.valueOf(instanceKey));

        } catch (Exception e) {
            log.debug("Failed to send Message_FileReceived: " + e.getMessage());
        }

        // get the original file name by reading the header using getHeader()
        String originalFileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String newFileName = dateFormat.format(date) + " " + originalFileName;

        log.info("Processing file: " + originalFileName);

        // set the new file name by writing to the header using setHeader()
        exchange.getIn().setHeader(Exchange.FILE_NAME, newFileName);
    }
}
