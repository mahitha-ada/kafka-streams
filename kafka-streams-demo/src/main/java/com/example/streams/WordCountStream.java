package com.example.streams;

import com.example.model.TextMessage;
import com.example.model.WordCount;
import com.example.serde.JsonSerde;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micronaut.configuration.kafka.streams.ConfiguredStreamBuilder;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Kafka Streams topology for real-time word counting.
 * 
 * This class defines the stream processing pipeline that:
 * 1. Reads text messages from the input topic
 * 2. Splits each message into individual words
 * 3. Groups words by their value
 * 4. Counts occurrences of each word
 * 5. Stores results in a state store for querying
 * 6. Outputs results to a topic for downstream processing
 */
@Factory
public class WordCountStream {
    
    public static final String INPUT_TOPIC = "text-messages";
    public static final String OUTPUT_TOPIC = "word-counts";
    public static final String WORD_COUNT_STORE = "word-count-store";

    private final ObjectMapper objectMapper;
    
    public WordCountStream() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Creates the Kafka Streams topology for word counting.
     * 
     * @param builder The configured stream builder from Micronaut
     * @return The configured KStream for text message processing
     */
    @Singleton
    @Named("default")
    public KStream<String, String> wordCountStream(ConfiguredStreamBuilder builder) {

        // Create JSON serdes for custom classes
        JsonSerde<TextMessage> textMessageSerde = new JsonSerde<>(TextMessage.class);
        JsonSerde<WordCount> wordCountSerde = new JsonSerde<>(WordCount.class);

        // Create the input stream from text messages topic - reading JSON strings
        KStream<String, String> textStream = builder.stream(
            INPUT_TOPIC,
            Consumed.with(Serdes.String(), Serdes.String())
        );

        // Process the stream: parse JSON, split words, count, and output
        textStream
                // Parse JSON string to TextMessage object and extract content
                .mapValues(jsonString -> {
                    try {
                        System.out.println("Processing JSON: " + jsonString);
                        // Parse the JSON string directly using ObjectMapper
                        TextMessage message = objectMapper.readValue(jsonString, TextMessage.class);
                        String content = message.getContent();
                        System.out.println("Extracted content: " + content);
                        return content;
                    } catch (Exception e) {
                        // Log and skip invalid messages
                        System.err.println("Error parsing message: " + e.getMessage());
                        e.printStackTrace();
                        return "";
                    }
                })
                // Filter out empty content
                .filter((key, content) -> content != null && !content.trim().isEmpty())
                // Extract words from each message content
                .flatMapValues(content -> Arrays.stream(
                    content.toLowerCase().split("\\W+"))
                    .filter(word -> !word.isEmpty() && word.length() > 2) // Filter short words
                    .collect(Collectors.toList()))
                
                // Use the word as the key for grouping
                .selectKey((key, word) -> word)
                
                // Group by word and count occurrences
                .groupByKey()
                .count(Materialized.as(WORD_COUNT_STORE))
                
                // Convert to output format
                .toStream()
                .mapValues((word, count) -> new WordCount(word, count))
                
                // Send results to output topic
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), wordCountSerde));

        return textStream;
    }
}

