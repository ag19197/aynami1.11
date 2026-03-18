package com.gs.ayanami.service;

import com.gs.ayanami.client.DifyClient;
import com.gs.ayanami.model.ChatRequest;
import com.gs.ayanami.model.ChatResponse;
import com.gs.ayanami.model.TripPlan;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ChatService {

    private final DifyClient difyClient;
    private final ObjectMapper objectMapper; // Spring Boot会自动提供

    public ChatService(DifyClient difyClient, ObjectMapper objectMapper) {
        this.difyClient = difyClient;
        this.objectMapper = objectMapper;
    }

    public ChatResponse chat(String query, String user, String conversationId) {
        ChatRequest request = new ChatRequest(query, user, conversationId);
        ChatResponse response = difyClient.sendMessage(request);

        String answer = response.getAnswer();
        if (answer != null && !answer.isEmpty()) {
            try {
                // 尝试提取 JSON 部分
                String json = extractJsonFromString(answer);
                TripPlan plan = objectMapper.readValue(json, TripPlan.class);
                response.setPlan(plan);
            } catch (Exception e) {
                e.printStackTrace(); // 打印详细错误，便于调试
            }
        }
        return response;
    }

    private String extractJsonFromString(String text) {
        // 找到第一个 { 和最后一个 }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        }
        // 如果没有找到，返回原文本（可能不是 JSON）
        return text;
    }
}