package com.github.alecmus.visibility_example.file;

import com.github.alecmus.visibility_example.process.CamundaVisibilityProcess;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/*
 * File Processor bean that implements the Processor functional interface.
 * This allows it to be used in the .process() chained method in the router's
 * configure() method.
 */
@Component
public class FileProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(FileProcessor.class);

    private final CamundaVisibilityProcess visibilityProcess;

    @Autowired
    public FileProcessor(CamundaVisibilityProcess visibilityProcess) {
        this.visibilityProcess = visibilityProcess;
    }

    @Override
    public void process(Exchange exchange) {
        // create correlationKey and add it as an exchange header
        final String correlationKey = UUID.randomUUID().toString();
        exchange.getIn().setHeader("correlationKey", correlationKey);

        // start process instance
        visibilityProcess.startProcess("Process_VisibilityProcess", correlationKey);

        // send file received message
        visibilityProcess.sendMessage("Message_FileReceived", correlationKey);

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
