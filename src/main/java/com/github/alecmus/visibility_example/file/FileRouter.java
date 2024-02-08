package com.github.alecmus.visibility_example.file;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
 * The router bean, extending the RouteBuilder.
 */
@Component
public class FileRouter extends RouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(FileRouter.class);

    private static final String SOURCE_FOLDER = "C:/software/test/source-folder";
    private static final String DESTINATION_FOLDER = "C:/software/test/destination-folder";

    FileProcessor fileProcessor;

    @Autowired
    public FileRouter(FileProcessor fileProcessor) {
        this.fileProcessor = fileProcessor;
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
                        String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
                        log.info("Done processing: " + fileName);
                    })
                .doCatch(Exception.class)
                    .process(exchange -> {
                        Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                        if (cause != null)
                            log.error("Exception occured: ", cause);
                    })
                .end();
    }
}
