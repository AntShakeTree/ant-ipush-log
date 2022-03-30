package com.ant.ipush.asyn;

//import com.hll.ipush.config.KafkaConfig;

import com.ant.ipush.kafka.KafkaAppenderConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class LazyProducer {
//    private final static PropertiesUtil propertiesUtil = new PropertiesUtil(new String[]{"kafka.config.properties","application.properties","application.yml"});
    private volatile Producer<String, byte[]> producer;
    private final KafkaAppenderConfig kafkaAppenderConfig;
    private String topic;
    private static Logger logger = LoggerFactory.getLogger(LazyProducer.class);

    public LazyProducer(KafkaAppenderConfig kafkaAppenderConfig) {
        this.kafkaAppenderConfig = kafkaAppenderConfig;
    }

    private final String default_topic = "kafka_message";
    private final String brokers = "127.0.0.1:9092";
    private final String acks = "1";
    private final String clientId = "hll-ipush-log-cid";


    public Producer<String, byte[]> get() {
        Producer<String, byte[]> result = this.producer;
        if (result == null) {
            synchronized (this) {
                result = this.producer;
                if (result == null) {
                    Producer<String, byte[]> producer = this.initialize();
                    this.producer = result = producer;
                }
            }
        }

        return result;
    }

    public Producer<String, byte[]> initialize() {
        Producer<String, byte[]> producer = null;
        try {
            producer = createProducer();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{}", e);
//            kafkaAppenderConfig.addError("error creating producer", e);
        }
        return producer;
    }


    // 创建懒生产者
    public Producer<String, byte[]> createProducer() {
        if (this.producer != null) {
            return this.producer;
        }
        HashMap producerConfig = new HashMap<>();

        String broker = PropertiesUtil.getProperties().getStringProperty("kafka.brokers");
        String ack = PropertiesUtil.getProperties().getStringProperty("kafka.acks");
        String cid = PropertiesUtil.getProperties().getStringProperty("kafka.clientId");

        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker == null ? this.brokers : broker);
        producerConfig.put(ProducerConfig.ACKS_CONFIG, ack == null ? this.acks : ack);
//            producerConfig.put(ProducerConfig.LINGER_MS_CONFIG, propertiesUtil.getIngerMs());
        producerConfig.put(ProducerConfig.CLIENT_ID_CONFIG, cid == null ? this.clientId : cid);
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        String topic = PropertiesUtil.getProperties().getStringProperty("kafka.topic");
        if (topic == null) {
            this.topic = default_topic;
        }
//        logger.info("{}", JSON.toJSONString(producerConfig));
        return new KafkaProducer<>(producerConfig);
    }

    public boolean isInitialized() {
        return producer != null;
    }

    public String getTopic() {

        return this.topic;
    }
}
