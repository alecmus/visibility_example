package com.github.alecmus.visibility_example.file;

import com.github.alecmus.visibility_example.process.CamundaProcess;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private final CamundaProcess camundaProcess;

    @Autowired
    public FileRouter(FileProcessor fileProcessor, CamundaProcess process) {
        this.fileProcessor = fileProcessor;
        this.camundaProcess = process;
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

                        // complete process file service task
                        camundaProcess.completeServiceTask("Task_ProcessFile");

                        log.info("Done processing: " + fileName);
                    })
                .doCatch(Exception.class)
                    .process(exchange -> {
                        Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);

                        // add exception and cause as process variables
                        Map<String, Object> variables = Map.ofEntries(
                          Map.entry("exception", exception.toString()),
                          Map.entry("cause", exception.getCause().toString())
                        );

                        // fail service task
                        camundaProcess.failServiceTask("Task_ProcessFile", "FileProcessingError", variables, true);

                        log.error("Exception occured: ", exception);
                    })
                .end();
    }
}
