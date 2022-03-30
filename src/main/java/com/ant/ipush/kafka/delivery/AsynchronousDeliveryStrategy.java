package com.ant.ipush.kafka.delivery;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.BufferExhaustedException;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.TimeoutException;

import java.util.concurrent.Future;

/**
 * 异步发送策略
 *
 * @since 0.0.1
 */
@Slf4j
public class AsynchronousDeliveryStrategy implements DeliveryStrategy {

    @Override
    public <K, V, E> boolean send(Producer<K, V> producer, ProducerRecord<K, V> record, final E event,
                                  final FailedDeliveryCallback<E> failedDeliveryCallback) {
        try {

            Future<RecordMetadata>  future=producer.send(record,
                    (metadata, exception) -> {
                if (exception != null) {
                    log.error("{}",exception);
                    failedDeliveryCallback.onFailedDelivery(event, exception);
                }
            });

            return true;
        } catch (BufferExhaustedException | TimeoutException e) {
            failedDeliveryCallback.onFailedDelivery(event, e);
            log.error("======================={}",e);

            return false;
        }
    }

}
