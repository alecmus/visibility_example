package com.github.alecmus.visibility_example.file;

import com.github.alecmus.visibility_example.process.Process;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * File Processor bean that implements the Processor functional interface.
 * This allows it to be used in the .process() chained method in the router's
 * configure() method.
 */
@Component
public class FileProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(FileProcessor.class);

    private final Process process;

    @Autowired
    public FileProcessor(Process process) {
        this.process = process;
    }

    @Override
    public void process(Exchange exchange) {
        // start process instance
        Process.Properties properties = process.startProcess("Process_VisibilityProcess");

        // add correlationKey and instanceKey as exchange headers
        exchange.getIn().setHeader("correlationKey", properties.getCorrelationKey());
        exchange.getIn().setHeader("instanceKey", properties.getInstanceKey());

        // send file received message
        process.sendMessage("Message_FileReceived", properties.getCorrelationKey());

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
