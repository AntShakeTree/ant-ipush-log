package com.ant.ipush.asyn;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.AppenderAttachableImpl;

import com.ant.ipush.kafka.delivery.FailedDeliveryCallback;
import com.lmax.disruptor.EventTranslator;

public class RingBufferLogEventTranslator implements
        EventTranslator<AsynLogEvent> {

    private ILoggingEvent loggingEvent;
    private AsyncKafkaLoggerAppender asyncKafkaLoggerAppender;
    private  AppenderAttachableImpl<ILoggingEvent> aai;
    private LazyProducer lazyProducer;
    private FailedDeliveryCallback<ILoggingEvent> failedDeliveryCallback;

    @Override
    public void translateTo(AsynLogEvent event, long sequence) {
        event.setValues(this.asyncKafkaLoggerAppender,this.loggingEvent,this.aai,this.lazyProducer,this.failedDeliveryCallback);
        clear();
    }

    public void setValues(AsyncKafkaLoggerAppender asyncKafkaLoggerAppender, ILoggingEvent loggingEvent, AppenderAttachableImpl appenderAttachable,LazyProducer lazyProducer,FailedDeliveryCallback failedDeliveryCallback){
        this.asyncKafkaLoggerAppender=asyncKafkaLoggerAppender;
        this.loggingEvent=loggingEvent;
        this.aai= appenderAttachable;
        this.lazyProducer=lazyProducer;
        this.failedDeliveryCallback=failedDeliveryCallback;
    }

    private void clear(){
        setValues(asyncKafkaLoggerAppender,null,null,lazyProducer,failedDeliveryCallback);
    }
}
