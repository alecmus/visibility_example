package com.github.alecmus.visibility_example.file;

import io.camunda.zeebe.client.ZeebeClient;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

/*
 * The router bean, extending the RouteBuilder.
 */
@Component
public class FileRouter extends RouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(FileRouter.class);

    private static final String SOURCE_FOLDER = "C:/software/test/source-folder";
    private static final String DESTINATION_FOLDER = "C:/software/test/destination-folder";

    private final FileProcessor fileProcessor;
    private final ZeebeClient zeebeClient;

    @Autowired
    public FileRouter(FileProcessor fileProcessor, ZeebeClient zeebeClient) {
        this.fileProcessor = fileProcessor;
        this.zeebeClient = zeebeClient;
    }

    /*
     * Override the configure() method in order to provide the route flow.
     */
    @Override
    public void configure() {
        /*
         * The route flow. Read files from the source folder, process them using the
         * FileProcessor, and send the results to a destination folder.
         */
        from("file://" + SOURCE_FOLDER + "?delete=false")
                .doTry()
                    .process(fileProcessor)
                    .to("file://" + DESTINATION_FOLDER)
                .endDoTry()
                    .process(exchange -> {
                        final String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
                        final String instanceUUID = exchange.getIn().getHeader("instanceUUID", String.class);

                        try {
                            // send file processed message to process
                            zeebeClient.newPublishMessageCommand()
                                    .messageName("Message_FileProcessed")
                                    .correlationKey(instanceUUID)
                                    .timeToLive(Duration.ofSeconds(1))
                                    .send().join();
                        } catch (Exception e) {
                            log.debug("Failed to send Message_FileProcessed: " + e.getMessage());
                        }

                        log.info("Done processing: " + fileName);
                    })
                .doCatch(Exception.class)
                    .process(exchange -> {
                        Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);

                        try {
                            final String instanceUUID = exchange.getIn().getHeader("instanceUUID", String.class);
                            final Long instanceKey = exchange.getIn().getHeader("instanceKey", Long.class);

                            // add cause as a process variable
                            zeebeClient.newSetVariablesCommand(instanceKey)
                                    .variables(Map.of("cause", cause.toString()))
                                    .send().join();

                            // send error processing file message to process
                            zeebeClient.newPublishMessageCommand()
                                    .messageName("Message_ErrorProcessingFile")
                                    .correlationKey(instanceUUID)
                                    .timeToLive(Duration.ofSeconds(1))
                                    .send().join();
                        } catch (Exception e) {
                            log.debug("Failed to send Message_ErrorProcessingFile: " + e.getMessage());
                        }

                        if (cause != null)
                            log.error("Exception occured: ", cause);
                    })
                .end();
    }
}
