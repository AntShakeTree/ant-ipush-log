package com.ant.ipush.delivery;//package com.hll.ipush.delivery;
//
//import ch.qos.logback.core.spi.AppenderAttachableImpl;
//import com.hll.ipush.event.AsyncLoggerDisruptor;
//
//
//public class DisruptorFailDeliveryCallback<E, A> implements FailedDeliveryCallback<E> {
//    private final AsyncLoggerDisruptor publisher;
//    private final Translator<E, A> translator;
//    private final AppenderAttachableImpl<E> aai;
//    public DisruptorFailDeliveryCallback(AsyncLoggerDisruptor publisher, Translator<E, A> translator, AppenderAttachableImpl<E> aai) {
//        this.publisher = publisher;
//        this.translator = translator;
//        this.aai = aai;
//    }
//
//    @Override
//    public void onFailedDelivery(E evt, Throwable throwable) {
//        this.aai.appendLoopOnAppenders(evt);
//    }
//}
