package com.gs.ayanami.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.ayanami.model.ChatRequest;
import com.gs.ayanami.model.ChatResponse;
import com.gs.ayanami.utils.RedisConversationCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class DifyClient {

    private final String apiUrl;
    private final String apiKey;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private final RedisConversationCache redisCache;

    public DifyClient(
            @Value("${dify.api.url}") String apiUrl,
            @Value("${dify.api.key}") String apiKey,
            ObjectMapper objectMapper,
            RedisConversationCache redisCache) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.webClient = WebClient.builder().build();
        this.objectMapper = objectMapper;
        this.redisCache = redisCache;
    }

    public ChatResponse sendMessage(ChatRequest request) {
        AtomicReference<String> finalAnswer = new AtomicReference<>("");
        AtomicReference<String> messageId = new AtomicReference<>();
        AtomicReference<String> conversationId = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Flux<String> stream = webClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class);

        stream.subscribe(
                chunk -> {
                    System.out.println("Raw chunk: " + chunk);
                    try {
                        JsonNode node = objectMapper.readTree(chunk);
                        String event = node.has("event") ? node.get("event").asText() : "";
                        System.out.println("Event type: " + event);

                        String content = "";
                        if ("agent_thought".equals(event) && node.has("thought")) {
                            content = node.get("thought").asText();
                        } else if ("agent_message".equals(event) && node.has("answer")) {
                            content = node.get("answer").asText();
                        }

                        if (!content.isEmpty()) {
                            // 累加内容到 finalAnswer（内存）
                            finalAnswer.accumulateAndGet(content, (old, newPart) -> old + newPart);
                            System.out.println("Appended content, current length: " + finalAnswer.get().length());

                            // 同时将片段写入 Redis（如果 conversationId 已获取）
                            if (conversationId.get() != null) {
                                redisCache.appendAnswer(conversationId.get(), content);
                            }
                        }

                        // 提取元数据（仅第一次设置）
                        if (node.has("message_id") && messageId.get() == null) {
                            messageId.set(node.get("message_id").asText());
                        }
                        if (node.has("conversation_id") && conversationId.get() == null) {
                            conversationId.set(node.get("conversation_id").asText());
                        }

                        if ("message_end".equals(event)) {
                            System.out.println(">>> message_end received, finalAnswer length: " + finalAnswer.get().length());
                            latch.countDown();
                        } else if ("error".equals(event)) {
                            System.err.println("Dify error: " + node);
                            latch.countDown();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    error.printStackTrace();
                    latch.countDown();
                },
                () -> {
                    System.out.println(">>> Stream onComplete, finalAnswer length: " + finalAnswer.get().length());
                    latch.countDown();
                }
        );

        try {
            boolean finished = latch.await(180, TimeUnit.SECONDS); // 从 30 秒改为 60 秒
            System.out.println("After await, finished=" + finished + ", finalAnswer length: " + finalAnswer.get().length());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 如果最终答案为空，尝试使用第一个非空内容（兜底）
        if (finalAnswer.get().isEmpty() && messageId.get() != null) {
            finalAnswer.set("{\"error\":\"No content received from Dify\"}");
        }

        ChatResponse response = new ChatResponse();
        response.setMessage_id(messageId.get());
        response.setConversation_id(conversationId.get());
        response.setAnswer(finalAnswer.get());
        return response;
    }
}