package com.ant.ipush.asyn;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import com.ant.ipush.domain.DaemonThreadFactory;
import com.ant.ipush.domain.WeakValueQueue;
import com.ant.ipush.kafka.KafkaAppenderConfig;

import com.ant.ipush.kafka.delivery.FailedDeliveryCallback;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;

import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
public class AsyncKafkaLoggerAppender<E> extends KafkaAppenderConfig<E> implements AppenderAttachable<E> {
    private final AsyncLoggerDisruptor loggerDisruptor;
    private final AppenderAttachableImpl<E> aai = new AppenderAttachableImpl<E>();
    private LazyProducer lazyProducer;
    private final ThreadLocal<RingBufferLogEventTranslator> threadLocalTranslator = new ThreadLocal<>();
    private final static WeakValueQueue<ILoggingEvent> weakvalue = new WeakValueQueue<>();

    private final Worker worker;
    private static Executor executor = Executors.newSingleThreadExecutor(new DaemonThreadFactory("AsyncLogger[AsyncKafkaLoggerAppender]"));

    public AsyncKafkaLoggerAppender() {
        this.loggerDisruptor = new AsyncLoggerDisruptor("AsyncKafkaLoggerAppender", this);
        worker = new Worker(weakvalue, 10000, this);


    }

    private final FailedDeliveryCallback<E> failedDeliveryCallback = new FailedDeliveryCallback<E>() {
        @Override
        public void onFailedDelivery(E evt, Throwable throwable) {
            // 如果失败时，将Event事件交付给同Logger的其他Appender处理
            aai.appendLoopOnAppenders(evt);
            weakvalue.push((ILoggingEvent) evt);

        }
    };


//    public void stop() {
//        if (start) {
//            start = false;
//        }
//    }

    @Override
    public void doAppend(E e) {
        // 如果是Kafka 组件日志则放入队列等待消费
//            super.doAppend(e);
        logWithThreadLocalTranslator((ILoggingEvent) e);
    }


    private void logWithThreadLocalTranslator(ILoggingEvent e) {
        // Implementation note: this method is tuned for performance. MODIFY
        // WITH CARE!
        final RingBufferLogEventTranslator translator = getCachedTranslator();
        initTranslator(translator, e);
        loggerDisruptor.enqueueLogMessageInfo(translator);
    }

    private void initTranslator(RingBufferLogEventTranslator translator, ILoggingEvent event) {
        if (lazyProducer == null && !started) {
            start();

        }
        translator.setValues(this, event, aai, lazyProducer, failedDeliveryCallback);
    }


    private RingBufferLogEventTranslator getCachedTranslator() {
        RingBufferLogEventTranslator result = threadLocalTranslator.get();
        if (result == null) {
            result = new RingBufferLogEventTranslator();
            threadLocalTranslator.set(result);
        }
        return result;
    }

    @Override
    protected void append(E e) {
        logWithThreadLocalTranslator((ILoggingEvent) e);
    }

    @Override
    public void addAppender(Appender<E> newAppender) {
        aai.addAppender(newAppender);
    }

    @Override
    public Iterator<Appender<E>> iteratorForAppenders() {
        return aai.iteratorForAppenders();
    }

    @Override
    public Appender<E> getAppender(String name) {
        return aai.getAppender(name);
    }

    @Override
    public boolean isAttached(Appender<E> appender) {
        return aai.isAttached(appender);
    }

    @Override
    public void detachAndStopAllAppenders() {
        aai.detachAndStopAllAppenders();
    }

    @Override
    public boolean detachAppender(Appender<E> appender) {
        return aai.detachAppender(appender);
    }

    @Override
    public boolean detachAppender(String name) {
        return aai.detachAppender(name);
    }


    @Override
    public void start() {
        if (!checkPrerequisites()) return;
        if (partition != null && partition < 0) {
            partition = null;
        }
        lazyProducer = new LazyProducer(this);
        lazyProducer.get();
        executor.execute(worker);
        super.start();


    }


    @Override
    public void stop() {
        super.stop();
        if (lazyProducer != null && lazyProducer.isInitialized()) {
            try {
                lazyProducer.get().close();
            } catch (KafkaException e) {
                this.addWarn("Failed to shut down kafka producer: " + e.getMessage(), e);
            }
            lazyProducer = null;
        }
    }

    private class Worker implements Runnable {
        private final WeakValueQueue<ILoggingEvent> weakValueQueue;
        private final int tick;
        private final AsyncKafkaLoggerAppender asyncKafkaLoggerAppender;

        private Worker(WeakValueQueue weakValueQueue, int tick, AsyncKafkaLoggerAppender asyncKafkaLoggerAppender) {
            this.weakValueQueue = weakValueQueue;
            this.tick = tick;
            this.asyncKafkaLoggerAppender = asyncKafkaLoggerAppender;
        }

        @SneakyThrows
        @Override
        public void run() {
            ILoggingEvent loggingEvent;

            while ((loggingEvent = weakValueQueue.take()) != null && started) {
                log.info("FailedDeliveryCallback##{}", loggingEvent.getMessage());
                Thread.sleep(tick);
                asyncKafkaLoggerAppender.logWithThreadLocalTranslator(loggingEvent);
            }
        }
    }
}
