package com.example.controller;

import com.example.model.TextMessage;
import com.example.service.MessageService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

@Controller("/api/messages")
public class MessageController {
    
    private static final Logger LOG = LoggerFactory.getLogger(MessageController.class);
    
    @Inject
    private MessageService messageService;

    @Post
    public HttpResponse<String> sendMessage(@Body MessageRequest request) {
        try {
            String messageId = UUID.randomUUID().toString();
            TextMessage message = new TextMessage(
                    messageId,
                    request.getContent(),
                    request.getUserId(),
                    Instant.now()
            );
            
            messageService.sendMessage(message);
            LOG.info("Message sent successfully: {}", messageId);
            
            return HttpResponse.ok("Message sent with ID: " + messageId);
        } catch (Exception e) {
            LOG.error("Error sending message", e);
            return HttpResponse.serverError("Failed to send message: " + e.getMessage());
        }
    }

    @Get("/health")
    public HttpResponse<String> health() {
        return HttpResponse.ok("Message service is healthy");
    }

    public static class MessageRequest {
        private String content;
        private String userId;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
}

