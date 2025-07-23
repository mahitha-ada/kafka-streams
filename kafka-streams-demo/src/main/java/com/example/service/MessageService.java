package com.example.service;

import com.example.model.TextMessage;
import com.example.streams.WordCountStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Singleton;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

@Singleton
public class MessageService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageService.class);

    private final KafkaProducer<String, String> kafkaProducer;
    private final ObjectMapper objectMapper;

    public MessageService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        
        // Configure Kafka producer directly
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("acks", "all");
        props.put("retries", 0);
        
        this.kafkaProducer = new KafkaProducer<>(props);
    }

    public void sendMessage(TextMessage message) {
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            LOG.debug("Sending message to Kafka: {}", messageJson);
            
            // Create producer record with null key and JSON string as value
            ProducerRecord<String, String> record = new ProducerRecord<>(
                WordCountStream.INPUT_TOPIC, 
                null, // key
                messageJson // value
            );
            
            // Send the message
            kafkaProducer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    LOG.error("Error sending message to Kafka", exception);
                } else {
                    LOG.debug("Message sent successfully to topic {} partition {} offset {}", 
                        metadata.topic(), metadata.partition(), metadata.offset());
                }
            });
            
        } catch (JsonProcessingException e) {
            LOG.error("Error serializing message to JSON", e);
            throw new RuntimeException("Failed to serialize message", e);
        }
    }
}