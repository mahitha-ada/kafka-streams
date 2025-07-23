package com.example.config;

import com.example.model.TextMessage;
import com.example.serde.JsonSerde;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

@Factory
public class KafkaConfig {
    
    @Bean
    @Singleton
    @Named("textMessageSerializer")
    public Serializer<TextMessage> textMessageSerializer() {
        return new JsonSerde<>(TextMessage.class).serializer();
    }
    
    @Bean
    @Singleton
    @Named("textMessageDeserializer")
    public Deserializer<TextMessage> textMessageDeserializer() {
        return new JsonSerde<>(TextMessage.class).deserializer();
    }
}
