package com.example.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;
import java.util.Map;

/**
 * Generic JSON Serde for custom classes using Jackson ObjectMapper.
 * This allows Kafka Streams to serialize/deserialize custom POJOs.
 */
public class JsonSerde<T> implements Serde<T> {
    
    private final Class<T> type;
    private final ObjectMapper objectMapper;
    
    public JsonSerde(Class<T> type) {
        this.type = type;
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public Serializer<T> serializer() {
        return new JsonSerializer<>();
    }
    
    @Override
    public Deserializer<T> deserializer() {
        return new JsonDeserializer<>(type);
    }
    
    private class JsonSerializer<T> implements Serializer<T> {
        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {
            // No configuration needed
        }
        
        @Override
        public byte[] serialize(String topic, T data) {
            if (data == null) {
                return null;
            }
            
            try {
                return objectMapper.writeValueAsBytes(data);
            } catch (Exception e) {
                throw new SerializationException("Error serializing JSON message", e);
            }
        }
        
        @Override
        public void close() {
            // Nothing to close
        }
    }
    
    private class JsonDeserializer<T> implements Deserializer<T> {
        private final Class<T> type;
        
        public JsonDeserializer(Class<T> type) {
            this.type = type;
        }
        
        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {
            // No configuration needed
        }
        
        @Override
        public T deserialize(String topic, byte[] data) {
            if (data == null) {
                return null;
            }
            
            try {
                return objectMapper.readValue(data, type);
            } catch (IOException e) {
                throw new SerializationException("Error deserializing JSON message", e);
            }
        }
        
        @Override
        public void close() {
            // Nothing to close
        }
    }
}

