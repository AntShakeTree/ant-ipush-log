package com.ant.ipush.http;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import com.alibaba.fastjson.JSON;
import com.ant.ipush.domain.LogAnalytics;
import com.ant.ipush.kafka.delivery.FailedDeliveryCallback;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpAppender extends HttpAppenderConfig<ILoggingEvent> {
    private static final String HTTP_LOGGER_PREFIX = HttpAppender.class.getPackage().getName().replaceFirst("\\.HTTP$", "");

    private final ConcurrentLinkedQueue<ILoggingEvent> queue = new ConcurrentLinkedQueue<ILoggingEvent>();
    private final AppenderAttachableImpl<ILoggingEvent> aai = new AppenderAttachableImpl<ILoggingEvent>();
    private final FailedDeliveryCallback<ILoggingEvent> failedDeliveryCallback = (evt, throwable) -> aai.appendLoopOnAppenders(evt);
    private final AtomicInteger atomicInteger = new AtomicInteger(0);
    private final Map<String, Object> header = new HashMap<>();

    public HttpAppender() {

        header.put("accessKey", this.accessKey);
        //根据AccessKey + Secretkey 生成Toten。
        header.put("token", this.token);


    }

    @Override
    public void start() {
        // only error free appenders should be activated
        if (!checkPrerequisites()) return;
        super.start();

    }


    /**
     * 利用队列，缓存输入。
     *
     * @param e
     */
    @Override
    public void doAppend(ILoggingEvent e) {
        deferAppend(e);
        ensureDeferredAppends();
    }


    @Override
    protected void append(ILoggingEvent e) {
        if (e.getMessage() == null) return;
        if (!e.getMessage().startsWith("{")) {
            failedDeliveryCallback.onFailedDelivery(e, null);
            addError(e.getFormattedMessage());
            return;
        }
        LogAnalytics logAnalytics = JSON.parseObject(e.getFormattedMessage(), LogAnalytics.class);
        logAnalytics.setTs(e.getTimeStamp());
        if (logAnalytics.getAType() == null) {
            failedDeliveryCallback.onFailedDelivery(e, null);
            addError(e.getFormattedMessage());
            return;
        }

        final Long timestamp = getTimestamp(e);
        String url = getServer();
        header.put("timestamp", timestamp);
        //TODO 正式发送埋点
        HttpRes httpRes = HttpUtil.getIntance().send(url, JSON.toJSONString(logAnalytics), header);
        //重试次数
        if (httpRes.isError()) {
            deferAppend(e);
            while (atomicInteger.incrementAndGet() == this.getRetryTime()) {
                failedDeliveryCallback.onFailedDelivery(e, null);
                addError(e.getFormattedMessage());
                return;
            }
        } else {
            atomicInteger.set(0);
        }


    }

    @Override
    public Iterator<Appender<ILoggingEvent>> iteratorForAppenders() {
        return aai.iteratorForAppenders();
    }

    @Override
    public Appender<ILoggingEvent> getAppender(String name) {
        return aai.getAppender(name);
    }

    @Override
    public boolean isAttached(Appender<ILoggingEvent> appender) {
        return aai.isAttached(appender);
    }

    @Override
    public void detachAndStopAllAppenders() {
        aai.detachAndStopAllAppenders();
    }

    @Override
    public boolean detachAppender(Appender<ILoggingEvent> appender) {
        return aai.detachAppender(appender);
    }

    @Override
    public boolean detachAppender(String name) {
        return aai.detachAppender(name);
    }

    // drains queue events to super
    private void ensureDeferredAppends() {

        while (queue.peek() != null) {
            super.doAppend(queue.poll());
        }
    }


    private void deferAppend(ILoggingEvent event) {
        queue.add(event);
    }

    protected Long getTimestamp(ILoggingEvent e) {
        if (e instanceof ILoggingEvent) {
            return e.getTimeStamp();
        } else {
            return System.currentTimeMillis();
        }
    }

    @Override
    public void addAppender(Appender<ILoggingEvent> newAppender) {
        aai.addAppender(newAppender);
    }


}
