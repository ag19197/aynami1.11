package com.gs.ayanami.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.ayanami.client.DifyClient;
import com.gs.ayanami.model.ChatRequest;
import com.gs.ayanami.model.ChatResponse;
import com.gs.ayanami.model.ConversationHistory;
import com.gs.ayanami.model.DifyResponse;
import com.gs.ayanami.repository.ConversationHistoryRepository;
import com.gs.ayanami.utils.RedisConversationCache;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ChatService {

    private final DifyClient difyClient;
    private final ObjectMapper objectMapper; // 原有的，可能用于其他地方

    private final RedisConversationCache redisCache;

    private final ConversationHistoryRepository historyRepository;

    public ChatService(DifyClient difyClient, ObjectMapper objectMapper,
                       RedisConversationCache redisCache,
                       ConversationHistoryRepository historyRepository) {
        this.difyClient = difyClient;
        this.objectMapper = objectMapper;
        this.redisCache = redisCache;
        this.historyRepository = historyRepository;
    }


    public ChatResponse chat(String query, String user, String conversationId) {
        // 1. 确保有 conversationId（如果前端未传则生成）
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString();
        }

        // 2. 初始化 Redis 临时缓存（写入用户输入和空的 answer）
        redisCache.init(conversationId, user, query);

        // 3. 调用 DifyClient（流式聚合，内部会不断追加内容到 Redis）
        ChatRequest request = new ChatRequest(query, user, conversationId);
        ChatResponse response = difyClient.sendMessage(request);

        // 4. 从 Redis 获取完整的 answer（如果 Redis 中有，则优先使用）
        String finalAnswer = redisCache.getAnswer(conversationId);
        if (finalAnswer == null || finalAnswer.isEmpty()) {
            // 如果 Redis 中没有（例如未启用 Redis 或写入失败），则回退到 response 中的 answer
            finalAnswer = response.getAnswer();
        } else {
            // 将完整的 answer 设置回 response 中，以便后续使用
            response.setAnswer(finalAnswer);
        }

        // 5. 保存到 MySQL 数据库
        ConversationHistory history = new ConversationHistory();
        history.setUserId(user);
        history.setConversationId(conversationId);
        history.setQuery(query);
        history.setAnswer(finalAnswer);
        history.setCreatedAt(LocalDateTime.now());
        historyRepository.save(history);

        // 6. 删除 Redis 临时缓存
        redisCache.delete(conversationId);

        // 7. 原有 JSON 解析逻辑（不变，但使用 finalAnswer 作为解析源）
        String answerToParse = finalAnswer;  // 使用从 Redis 获取的完整答案
        if (answerToParse != null && !answerToParse.isEmpty()) {
            String cleanedAnswer = cleanJsonString(answerToParse);
            try {
                ObjectMapper lenientMapper = new ObjectMapper();
                lenientMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                DifyResponse difyResp = lenientMapper.readValue(cleanedAnswer, DifyResponse.class);
                response.setPlan(difyResp.getMap_data());
                response.setRichText(difyResp.getText_plan());
            } catch (Exception e) {
                // 原有的修复逻辑（保持不变）
                if (e instanceof JsonMappingException && e.getMessage().contains("expected closing quote for a string value")) {
                    String fixedAnswer = cleanedAnswer + "\"}";
                    try {
                        ObjectMapper lenientMapper = new ObjectMapper();
                        lenientMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                        DifyResponse difyResp = lenientMapper.readValue(fixedAnswer, DifyResponse.class);
                        response.setPlan(difyResp.getMap_data());
                        response.setRichText(difyResp.getText_plan());
                        System.out.println("JSON 修复成功");
                    } catch (Exception ex) {
                        System.err.println("JSON 修复失败，原始错误：");
                        e.printStackTrace();
                        response.setRichText(answerToParse);
                    }
                } else {
                    e.printStackTrace();
                    response.setRichText(answerToParse);
                }
            }
        }

        // 8. 返回响应
        return response;
    }



    //清洗JSON
    // 在 ChatService 类中添加清洗方法
    private String cleanJsonString(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        // 去除 Markdown 代码块
        if (trimmed.startsWith("```json") && trimmed.endsWith("```")) {
            trimmed = trimmed.substring(7, trimmed.length() - 3).trim();
        } else if (trimmed.startsWith("```") && trimmed.endsWith("```")) {
            trimmed = trimmed.substring(3, trimmed.length() - 3).trim();
        }

        // 提取第一个 '{' 到最后一个 '}' 之间的部分
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }
}