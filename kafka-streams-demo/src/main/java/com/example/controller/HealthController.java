package com.example.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import java.time.Instant;
import java.util.Map;

@Controller("/")
public class HealthController {

    @Get
    public HttpResponse<Map<String, Object>> index() {
        return HttpResponse.ok(Map.of(
                "message", "Micronaut + Kafka Streams Demo",
                "status", "running",
                "timestamp", Instant.now()
        ));
    }

    @Get("/health")
    public HttpResponse<Map<String, String>> health() {
        return HttpResponse.ok(Map.of(
                "status", "UP",
                "service", "micronaut-kafka-streams-demo"
        ));
    }
}

