package com.example.controller;

import com.example.model.WordCount;
import com.example.service.WordCountService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Controller("/api/wordcounts")
public class WordCountController {
    
    private static final Logger LOG = LoggerFactory.getLogger(WordCountController.class);
    
    @Inject
    private WordCountService wordCountService;

    @Get("/{word}")
    public HttpResponse<WordCount> getWordCount(@PathVariable String word) {
        try {
            Optional<WordCount> wordCount = wordCountService.getWordCount(word.toLowerCase());
            if (wordCount.isPresent()) {
                return HttpResponse.ok(wordCount.get());
            } else {
                return HttpResponse.ok(new WordCount(word, 0L));
            }
        } catch (Exception e) {
            LOG.error("Error getting word count for: " + word, e);
            return HttpResponse.serverError();
        }
    }

    @Get
    public HttpResponse<List<WordCount>> getTopWords(@QueryValue Optional<Integer> limit) {
        try {
            int maxResults = limit.orElse(10);
            List<WordCount> topWords = wordCountService.getTopWords(maxResults);
            return HttpResponse.ok(topWords);
        } catch (Exception e) {
            LOG.error("Error getting top words", e);
            return HttpResponse.serverError();
        }
    }

    @Get("/health")
    public HttpResponse<String> health() {
        return HttpResponse.ok("Word count service is healthy");
    }
}

