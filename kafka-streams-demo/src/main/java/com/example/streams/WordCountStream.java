package com.example.streams;

import com.example.model.TextMessage;
import com.example.model.WordCount;
import com.example.serde.JsonSerde;
import io.micronaut.configuration.kafka.streams.ConfiguredStreamBuilder;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
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

    /**
     * Creates the Kafka Streams topology for word counting.
     * 
     * @param builder The configured stream builder from Micronaut
     * @return The configured StreamsBuilder
     */
    @Singleton
    @Named("word-count-stream")
    StreamsBuilder wordCountStream(ConfiguredStreamBuilder builder) {

        // Create JSON serdes for custom classes
        JsonSerde<TextMessage> textMessageSerde = new JsonSerde<>(TextMessage.class);
        JsonSerde<WordCount> wordCountSerde = new JsonSerde<>(WordCount.class);

        // Create the input stream from text messages topic
        KStream<String, TextMessage> textStream = builder.stream(
            INPUT_TOPIC,
            Consumed.with(Serdes.String(), textMessageSerde)
        );

        // Process the stream: split words, count, and output
        textStream
                // Extract words from each message content
                .flatMapValues(message -> Arrays.stream(
                    message.getContent().toLowerCase().split("\\W+"))
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

        return builder;
    }
}

