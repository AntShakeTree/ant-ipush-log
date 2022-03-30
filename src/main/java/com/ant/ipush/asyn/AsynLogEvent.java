package com.ant.ipush.asyn;


import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.AppenderAttachableImpl;

import com.ant.ipush.domain.EventMessageData;
import com.ant.ipush.domain.JSON;
import com.ant.ipush.kafka.delivery.FailedDeliveryCallback;
import com.ant.ipush.log.CachingSupplier;
import com.lmax.disruptor.EventFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.event.LoggingEvent;

import java.time.Clock;

@Slf4j
public class AsynLogEvent<E> {
    public static final EventFactory<AsynLogEvent> FACTORY = () -> new AsynLogEvent();
    private E loggingEvent;
    private AsyncKafkaLoggerAppender asyncKafkaLoggerAppender;
    private AppenderAttachableImpl<E> aai;
    private LazyProducer lazyProducer;
    private boolean endOfBatch;

    private FailedDeliveryCallback failedDeliveryCallback;


    public void setValues(final AsyncKafkaLoggerAppender asyncKafkaLoggerAppender, final E loggingEvent, final AppenderAttachableImpl<E> aai, final LazyProducer lazyProducer, final FailedDeliveryCallback failedDeliveryCallback) {
        this.loggingEvent = loggingEvent;
        this.asyncKafkaLoggerAppender = asyncKafkaLoggerAppender;
        this.aai = aai;
        this.lazyProducer = lazyProducer;
        this.failedDeliveryCallback = failedDeliveryCallback;

    }

    public void execute(boolean endOfBatch) {
        this.endOfBatch = endOfBatch;
        if (loggingEvent != null) {
            final E iLoggingEvent = loggingEvent;
            this.actualAsyncLog(iLoggingEvent);
        } else {
            asyncKafkaLoggerAppender.addError("E is null.");
        }
    }

    private void actualAsyncLog(E iLoggingEvent) {
        try {
            final Producer<String, byte[]> producer = lazyProducer.get();
            final Long timestamp = getTimestamp(iLoggingEvent);
            final ProducerRecord<String, byte[]> record;
            // 将MSG以JSON字节的方式格式化发送
            String val = null;
            if (iLoggingEvent instanceof LoggingEvent) {
                val = ((LoggingEvent) loggingEvent).getMessage();
            } else if (iLoggingEvent instanceof ILoggingEvent) {
                val = ((ILoggingEvent) loggingEvent).getMessage();
            } else {
                asyncKafkaLoggerAppender.addInfo(iLoggingEvent.toString());
                return;
            }
            val = val.substring(val.indexOf("{"), val.lastIndexOf("}") + 1);
            EventMessageData eventMessageData = JSON.parseObject(val, EventMessageData.class);
            if (eventMessageData.getPayload().getTopicName() == null || "".equals(eventMessageData.getPayload().getTopicName())) {
                eventMessageData.getPayload().setTopicName(lazyProducer.getTopic());
                record = new ProducerRecord(lazyProducer.getTopic(), null, timestamp, eventMessageData.getIdentifier(), JSON.toJSONBytes(eventMessageData));
            } else
                record = new ProducerRecord(eventMessageData.getPayload().getTopicName(), null, timestamp, eventMessageData.getIdentifier(), JSON.toJSONBytes(eventMessageData));
            if (producer != null && asyncKafkaLoggerAppender.getDeliveryStrategy() != null) {
                asyncKafkaLoggerAppender.getDeliveryStrategy().send(producer, record, iLoggingEvent, failedDeliveryCallback);
            } else {
                failedDeliveryCallback.onFailedDelivery(iLoggingEvent, null);
            }
        } catch (Exception exception) {
            log.error("{}", exception);
            failedDeliveryCallback.onFailedDelivery(loggingEvent, null);
        }
    }

    CachingSupplier<Clock> cachingSupplier = CachingSupplier.of(Clock.systemUTC());

    // 获取事件时间戳
    protected Long getTimestamp(E e) {
        if (e instanceof ILoggingEvent) {
            return ((ILoggingEvent) e).getTimeStamp();
        }
        return cachingSupplier.get().millis();
    }

    public void clear() {
        setValues(asyncKafkaLoggerAppender, null, null, lazyProducer, failedDeliveryCallback);
    }


}
