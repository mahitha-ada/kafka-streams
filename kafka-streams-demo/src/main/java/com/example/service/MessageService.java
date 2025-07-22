package com.example.service;

import com.example.model.TextMessage;
import com.example.streams.WordCountStream;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MessageService {
    
    private static final Logger LOG = LoggerFactory.getLogger(MessageService.class);
    
    private final MessageProducer messageProducer;

    public MessageService(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    public void sendMessage(TextMessage message) {
        LOG.debug("Sending message to Kafka: {}", message);
        messageProducer.sendMessage(message.getId(), message);
    }

    @KafkaClient
    public interface MessageProducer {
        @Topic(WordCountStream.INPUT_TOPIC)
        void sendMessage(String key, TextMessage message);
    }
}

