package com.ant.ipush.kafka;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class LogbackIntegrationIT {

    private static final Charset UTF8 = StandardCharsets.UTF_8;
    @Rule
    public ErrorCollector collector = new ErrorCollector();
    private org.slf4j.Logger logger;

    @Before
    public void beforeLogSystemInit() throws IOException, InterruptedException {
        logger = LoggerFactory.getLogger("LogbackIntegrationIT");

    }

    @Test
    public void testLogging() {

        for (int i = 0; i < 1000; ++i) {
            logger.info("message" + (i));
        }


    }

}
