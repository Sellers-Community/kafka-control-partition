package org.tradedigital.kafkacontrolpartition.config;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.GenericMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaOperations;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;

import java.util.Iterator;

public class PartitionAwareReplyingKafkaTemplate<K, V, R> extends ReplyingKafkaTemplate<K, V, R> implements ReplyingKafkaOperations<K, V, R> {

    public PartitionAwareReplyingKafkaTemplate(ProducerFactory<K, V> producerFactory, GenericMessageListenerContainer<K, R> replyContainer) {

        super(producerFactory, replyContainer);
    }

    protected RequestReplyFuture<K, V, R> doSendAndReceive(ProducerRecord<K, V> record) {

        String replyTopic = getReplyTopicHeader(record);

        if (replyTopic == null) {

            throw new KafkaException("Illegal argument: No reply topic is configured.");
        }

        TopicPartition replyPartition = getAssignedReplyTopicPartition(replyTopic);

        if (replyPartition == null) {

            throw new KafkaException("Illegal state: Reply topic is not match to any partition.");
        }

        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, replyPartition.topic().getBytes()));
        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_PARTITION, intToBytesBigEndian(replyPartition.partition())));

        return super.sendAndReceive(record);
    }

    private static byte[] intToBytesBigEndian(int data) {

        return new byte[]{(byte)(data >> 24 & 255), (byte)(data >> 16 & 255), (byte)(data >> 8 & 255), (byte)(data & 255)};
    }

    private TopicPartition getAssignedReplyTopicPartition(String topicReplyName) {

        if (getAssignedReplyTopicPartitions() != null) {

            return getReplyTopicPartition(topicReplyName);
        } else {
            throw new KafkaException("Illegal state: No reply partition is assigned to this instance.");
        }
    }

    private TopicPartition getReplyTopicPartition(String topicReplyName) {

        Iterator<TopicPartition> topicPartitionIterator = getAssignedReplyTopicPartitions().iterator();

        while (topicPartitionIterator.hasNext()) {

            TopicPartition topicPartition = topicPartitionIterator.next();

            if (topicPartition.topic().equalsIgnoreCase(topicReplyName)) {

                return topicPartition;
            }
        }

        return null;
    }

    private String getReplyTopicHeader(ProducerRecord<K, V> record) {

        Iterator<Header> iterator = record.headers().iterator();

        while (iterator.hasNext()) {

            Header header = iterator.next();

            if (header.key().equalsIgnoreCase(KafkaHeaders.REPLY_TOPIC)) {

                return new String(header.value());
            }
        }

        return null;
    }
}
