package com.gs.ayanami.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.ayanami.client.DifyClient;
import com.gs.ayanami.model.ChatRequest;
import com.gs.ayanami.model.ChatResponse;
import com.gs.ayanami.model.DifyResponse;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final DifyClient difyClient;
    private final ObjectMapper objectMapper; // 原有的，可能用于其他地方

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
                ObjectMapper lenientMapper = new ObjectMapper();
                lenientMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                DifyResponse difyResp = lenientMapper.readValue(answer, DifyResponse.class);
                response.setPlan(difyResp.getMap_data());
                response.setRichText(difyResp.getText_plan());
            }catch (Exception e) {
                // 检查是否是字符串未闭合的错误
                if (e instanceof JsonMappingException && e.getMessage().contains("expected closing quote for a string value")) {
                    // 尝试修复：在末尾添加缺失的引号和大括号
                    String fixedAnswer = answer + "\"}";
                    try {
                        ObjectMapper lenientMapper = new ObjectMapper();
                        lenientMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                        DifyResponse difyResp = lenientMapper.readValue(fixedAnswer, DifyResponse.class);
                        response.setPlan(difyResp.getMap_data());
                        response.setRichText(difyResp.getText_plan());
                        System.out.println("JSON 修复成功");
                        return response;
                    } catch (Exception ex) {
                        // 修复失败，记录原始错误
                        System.err.println("JSON 修复失败，原始错误：");
                        e.printStackTrace();
                        response.setRichText(answer); // 至少显示原始内容
                    }
                } else {
                    // 其他解析错误
                    e.printStackTrace();
                    response.setRichText(answer);
                }
            }
        }
        return response;
    }
}