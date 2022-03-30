package com.ant.ipush.log;

import com.alibaba.fastjson.JSON;
import com.ant.ipush.domain.EventMessageData;

import com.ant.ipush.domain.MessagePayload;
import org.slf4j.Logger;

import java.util.*;

public class LogContext {

    static ThreadLocal<LogContext> LOCAL = new ThreadLocal<>();
    
    private EventMessageData eventMessageData;
    //    private Logger recorder;
    private static Object lock = new Object();

    private Logger logger;

    private LogContext() {
    }

    public static LogContext instance(Logger logger) {

        if (LOCAL.get() == null) {
            synchronized (lock) {
                EventMessageData eventMessageData = new EventMessageData();
                MessagePayload messagePayload = new MessagePayload();
                eventMessageData.setPayload(messagePayload);
                LogContext logContext = new LogContext();
                Map<String, Object> mapper = new HashMap<>();
                logContext.setEventMessageData(eventMessageData);
                eventMessageData.setMetaData(mapper);
                logContext.log(logger);
                LOCAL.set(logContext);
            }

        }
        return LOCAL.get();
    }

    public static Optional<LogContext> find() {
        return Optional.ofNullable(LOCAL.get());
    }

    public static void clear() {
        if (LOCAL.get() != null) {
            LOCAL.get().getEventMessageData().getMetaData().clear();
            LOCAL.get().setEventMessageData(null);
        }
        LOCAL.remove();
    }

    private void setEventMessageData(EventMessageData o) {
        this.eventMessageData = o;
    }

    private EventMessageData getEventMessageData() {
        return this.eventMessageData;
    }


    public LogContext appender(String key, String value) {
        this.getEventMessageData().getMetaData().put(key, value);
        return this;
    }

    public void info() {
        infoMapper();
    }

//
//    public void info(String json) {
//        Objects.requireNonNull(this.logger);
//        this.logger.info(json);
//        LogContext.clear();
//    }


    private void infoMapper() {
        Objects.requireNonNull(this.logger);
        Objects.requireNonNull(this.eventMessageData);
        Objects.requireNonNull(this.eventMessageData.getPayload().getEvent(), "Event is required.");
        Objects.requireNonNull(this.eventMessageData.getPayload().getTopicName(), "Topic name is required.");
        if (this.eventMessageData.getIdentifier() == null || "".equals(this.eventMessageData.getIdentifier())) {
            eventMessageData.setIdentifier(UUID.randomUUID().toString());
        }
        this.logger.info(JSON.toJSONString(eventMessageData));
//        if (this.recorder!=null){
//            this.recorder.info("{}",JSON.toJSONString(eventMessageData));
//        }
        LogContext.clear();
    }


    protected LogContext mapper(Map<String, Object> mapper) {
        this.getEventMessageData().setMetaData(mapper);
        return this;
    }

    private LogContext log(Logger logger) {
        this.logger = logger;
        return this;
    }

    public LogContext event(String event) {
        this.event(event, null);
        return this;
    }

    public LogContext identifier(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        if (id.trim().length() == 0) {
            throw new IllegalArgumentException("id is empty");
        }

        this.eventMessageData.setIdentifier(id);
        return this;
    }

    public LogContext topic(String topic) {
        this.getEventMessageData().getPayload().setTopicName(topic);
        return this;
    }

    public LogContext event(String event, String scene) {
        this.getEventMessageData().event(event, scene);
        return this;
    }

    public LogContext event(String event, String scene, String topic) {
        this.getEventMessageData().event(event, scene);
        this.getEventMessageData().getPayload().setTopicName(topic);
        return this;
    }

    public LogContext biz(String biz) {
        this.getEventMessageData().getPayload().setBiz(biz);

        return this;
    }

    public LogContext event(String event, String scene, String topic, String biz) {
        this.getEventMessageData().event(event, scene);
        this.getEventMessageData().getPayload().setTopicName(topic);
        this.getEventMessageData().getPayload().setBiz(biz);
        return this;
    }

    public LogContext appenderAll(Map<String, Object> map) {
        this.getEventMessageData().mergeMetaData(map);
        return this;
    }

    //    public LogContext recorder(Logger logger){
//        this.logger=logger;
//        return this;
//    }
//    public static void main(String[] args) {
//    }
}