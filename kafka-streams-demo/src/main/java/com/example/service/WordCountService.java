package com.example.service;

import com.example.model.WordCount;
import com.example.streams.WordCountStream;
import io.micronaut.configuration.kafka.streams.InteractiveQueryService;
import jakarta.inject.Singleton;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Singleton
public class WordCountService {
    
    private static final Logger LOG = LoggerFactory.getLogger(WordCountService.class);
    
    private final InteractiveQueryService interactiveQueryService;

    public WordCountService(InteractiveQueryService interactiveQueryService) {
        this.interactiveQueryService = interactiveQueryService;
    }

    public Optional<WordCount> getWordCount(String word) {
        try {
            ReadOnlyKeyValueStore<String, Long> store = getWordCountStore();
            if (store != null) {
                Long count = store.get(word);
                if (count != null) {
                    return Optional.of(new WordCount(word, count));
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            LOG.error("Error getting word count for: " + word, e);
            return Optional.empty();
        }
    }

    public List<WordCount> getTopWords(int limit) {
        List<WordCount> wordCounts = new ArrayList<>();
        try {
            ReadOnlyKeyValueStore<String, Long> store = getWordCountStore();
            if (store != null) {
                try (KeyValueIterator<String, Long> iterator = store.all()) {
                    while (iterator.hasNext() && wordCounts.size() < limit * 10) { // Get more to sort
                        var keyValue = iterator.next();
                        wordCounts.add(new WordCount(keyValue.key, keyValue.value));
                    }
                }
                
                // Sort by count descending and limit results
                wordCounts.sort(Comparator.comparing(WordCount::getCount).reversed());
                return wordCounts.stream().limit(limit).toList();
            }
        } catch (Exception e) {
            LOG.error("Error getting top words", e);
        }
        return wordCounts;
    }

    private ReadOnlyKeyValueStore<String, Long> getWordCountStore() {
        try {
            ReadOnlyKeyValueStore<String, Long> store = interactiveQueryService.getQueryableStore(
                    WordCountStream.WORD_COUNT_STORE,
                    QueryableStoreTypes.<String, Long>keyValueStore()
            ).orElse(null);
            return store;
        } catch (Exception e) {
            LOG.warn("Word count store not available yet: {}", e.getMessage());
            return null;
        }
    }
}

