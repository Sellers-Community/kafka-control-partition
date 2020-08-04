package org.tradedigital.kafkacontrolpartition.config;

import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.concurrent.CompletableFuture;

public interface CompletableFutureReplyingKafkaOperations<K, V, R> {
    CompletableFuture<R> requestReply(ProducerRecord<K, V> record);
}
