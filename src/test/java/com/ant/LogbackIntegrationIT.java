package com.ant;


import com.ant.ipush.domain.LogAnalytics;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class LogbackIntegrationIT {

    @Rule
    public ErrorCollector collector = new ErrorCollector();


    private org.slf4j.Logger logger;

    @Before
    public void beforeLogSystemInit() throws IOException, InterruptedException {

        logger = LoggerFactory.getLogger("LogbackIntegrationIT");

    }

    @After
    public void tearDown() {


    }


    @Test
    public void testLogging() {

        for (int i = 0; i < 1000; ++i) {
            LogAnalytics logAnalytics=new LogAnalytics();
            logAnalytics.setAType("xx"+i);
            logger.info(logAnalytics.toString());
        }
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("enable.auto.commit", "false");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", ByteArrayDeserializer.class.getName());
        props.put("value.deserializer", ByteArrayDeserializer.class.getName());


        final KafkaConsumer<byte[], byte[]> client = new KafkaConsumer<>(props);
        client.assign(Collections.singletonList(new TopicPartition("header2", 0)));
        client.seekToBeginning(Collections.singletonList(new TopicPartition("header2", 0)));

        int no = 0;

        ConsumerRecords<byte[], byte[]> poll = client.poll(1000);
        while (!poll.isEmpty()) {
            for (ConsumerRecord<byte[], byte[]> consumerRecord : poll) {
                final String messageFromKafka = new String(consumerRecord.key(), UTF8);
//                final int messageFromKafka = byteArrayToInt(consumerRecord.key());
//                assertThat(messageFromKafka, Matchers.equalTo("message"+no));
                System.out.println(messageFromKafka + "====================");
                ++no;
            }
            poll = client.poll(1000);
        }

        assertTrue(no > 0);

    }

    private static final Charset UTF8 = Charset.forName("UTF-8");
    //byte 数组与 int 的相互转换
    public static int byteArrayToInt(byte[] b) {
        return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

}
