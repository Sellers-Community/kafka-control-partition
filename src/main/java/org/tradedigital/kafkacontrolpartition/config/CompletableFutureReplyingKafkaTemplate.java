package org.tradedigital.kafkacontrolpartition.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.GenericMessageListenerContainer;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.CompletableFuture;

public class CompletableFutureReplyingKafkaTemplate<K, V, R> extends PartitionAwareReplyingKafkaTemplate<K, V, R> implements CompletableFutureReplyingKafkaOperations<K, V, R> {
    public CompletableFutureReplyingKafkaTemplate(ProducerFactory<K, V> producerFactory, GenericMessageListenerContainer<K, R> replyContainer) {
        super(producerFactory, replyContainer);
    }

    public CompletableFuture<R> requestReply(ProducerRecord<K, V> record) {
        return adapt(doSendAndReceive(record));
    }

    private CompletableFuture<R> adapt(final RequestReplyFuture<K, V, R> requestReplyFuture) {
        final CompletableFuture<R> completableResult = new CompletableFuture<R>() {
            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean result = requestReplyFuture.cancel(mayInterruptIfRunning);
                super.cancel(mayInterruptIfRunning);
                return result;
            }
        };
        requestReplyFuture.addCallback(new ListenableFutureCallback<ConsumerRecord<K, R>>() {
            public void onSuccess(ConsumerRecord<K, R> result) {
                completableResult.complete(result.value());
            }

            public void onFailure(Throwable t) {
                completableResult.completeExceptionally(t);
            }
        });
        return completableResult;
    }
}
