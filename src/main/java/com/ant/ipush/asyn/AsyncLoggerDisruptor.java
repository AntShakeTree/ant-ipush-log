package com.ant.ipush.asyn;


import com.ant.ipush.domain.DaemonThreadFactory;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Getter
public class AsyncLoggerDisruptor {
    private static final int SLEEP_MILLIS_BETWEEN_DRAIN_ATTEMPTS = 50;
    private static final int MAX_DRAIN_ATTEMPTS_BEFORE_SHUTDOWN = 200;
    private static final int RING_SIZE = 1024;
    private volatile Disruptor<AsynLogEvent> disruptor;
    private volatile boolean start = false;
    private ThreadFactory threadFactory;

    private ExecutorService executor;
    private long backgroundThreadId;
    private final String contextName;
    private final AsyncKafkaLoggerAppender asyncKafkaLoggerAppender;
    Logger LOGGER = LoggerFactory.getLogger(AsyncLoggerDisruptor.class);


    public AsyncLoggerDisruptor(String contextName, AsyncKafkaLoggerAppender asyncKafkaLoggerAppender) {
        this.contextName = contextName;
        this.asyncKafkaLoggerAppender = asyncKafkaLoggerAppender;
        start();
    }

    boolean shouldLogInCurrentThread() {
        return currentThreadIsAppenderThread() && isRingBufferFull();
    }

    private boolean isRingBufferFull() {
        final Disruptor<AsynLogEvent> theDisruptor = this.disruptor;
        return theDisruptor == null || theDisruptor.getRingBuffer().remainingCapacity() == 0;
    }

    private boolean currentThreadIsAppenderThread() {
        return Thread.currentThread().getId() == backgroundThreadId;
    }

    void enqueueLogMessageInfo(final RingBufferLogEventTranslator translator) {
        try {
            disruptor.publishEvent(translator);
        } catch (final NullPointerException npe) {
            asyncKafkaLoggerAppender.addError(" Ignoring log event after log4j was shut down.");
        }
    }


    Disruptor<AsynLogEvent> getDisruptor() {
        return disruptor;
    }


    private void setExceptionHandler(ExceptionHandler<? super AsynLogEvent> exceptionHandler) {
        disruptor.setDefaultExceptionHandler(exceptionHandler);
    }

    private static boolean hasBacklog(final Disruptor<?> theDisruptor) {
        final RingBuffer<?> ringBuffer = theDisruptor.getRingBuffer();
        return !ringBuffer.hasAvailableCapacity(ringBuffer.getBufferSize());
    }


    public synchronized void start() {
        if (disruptor != null) {
            LOGGER.error(
                    "[{}] AsyncLoggerDisruptor not starting new disruptor for this context, using existing object.", contextName);
            return;
        }
        LOGGER.info("[{}] AsyncLoggerDisruptor creating new disruptor for this context.", contextName);
        final int ringBufferSize = DisruptorUtil.calculateRingBufferSize("AsyncLogger.RingBufferSize");
        final WaitStrategy waitStrategy = DisruptorUtil.createWaitStrategy("AsyncLogger.WaitStrategy");
        threadFactory = new DaemonThreadFactory("AsyncLogger[" + contextName + "]");
        executor = Executors.newSingleThreadExecutor(threadFactory);
        backgroundThreadId = DisruptorUtil.getExecutorThreadId(executor);
        disruptor = new Disruptor<>(AsynLogEvent.FACTORY, ringBufferSize, executor, ProducerType.MULTI,
                waitStrategy);

        disruptor.handleExceptionsWith(loggerExceptionHandler);

        final LogEventHandler[] handlers = {new LogEventHandler()};
        disruptor.handleEventsWith(handlers);
        disruptor.start();

    }


    private final LoggerExceptionHandler loggerExceptionHandler = new LoggerExceptionHandler();


    private final class LoggerExceptionHandler implements ExceptionHandler<AsynLogEvent> {

        @Override
        public void handleEventException(Throwable ex, long sequence, AsynLogEvent event) {
            LOGGER.error("AsyncLogger.ExceptionHandler[{}]-[{}]", ex, event);
        }

        @Override
        public void handleOnStartException(Throwable ex) {
            LOGGER.error("AsyncLogger.ExceptionHandler-handleOnStartException[{}]", ex);
            
        }

        @Override
        public void handleOnShutdownException(Throwable ex) {
            LOGGER.error("AsyncLogger.ExceptionHandler-handleOnShutdownException[{}]", ex);

        }
    }

//    private Map<String, Object> producerConfig = new HashMap<>();

    // 创建懒生产者
//    protected Producer<String, byte[]> createProducer() {
//        KafkaConfig kafkaConfig = SpringContextUtil.getBean(KafkaConfig.class);
//        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBrokers());
//        producerConfig.put(ProducerConfig.ACKS_CONFIG, kafkaConfig.getAcks());
//        producerConfig.put(ProducerConfig.LINGER_MS_CONFIG, kafkaConfig.getIngerMs());
//        producerConfig.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaConfig.getClientId());
//        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
//        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
//        return new KafkaProducer<>(new HashMap<>(producerConfig));
//    }


}