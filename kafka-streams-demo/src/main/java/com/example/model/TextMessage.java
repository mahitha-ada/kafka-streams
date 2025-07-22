package com.example.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;

import java.time.Instant;

@Introspected
public class TextMessage {
    private final String id;
    private final String content;
    private final String userId;
    private final Instant timestamp;

    @JsonCreator
    public TextMessage(@JsonProperty("id") String id, 
                      @JsonProperty("content") String content,
                      @JsonProperty("userId") String userId,
                      @JsonProperty("timestamp") Instant timestamp) {
        this.id = id;
        this.content = content;
        this.userId = userId;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getUserId() {
        return userId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "TextMessage{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", userId='" + userId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}

