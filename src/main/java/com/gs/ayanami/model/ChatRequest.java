package com.gs.ayanami.model;

import java.util.Map;
import java.util.HashMap;

public class ChatRequest {
    private String query;
    private String user;
    private String conversationId;
    private String response_mode;
    private Map<String, Object> inputs;  // 新增

    public ChatRequest() {
    }

    public ChatRequest(String query, String user, String conversationId) {
        this.query = query;
        this.user = user;
        this.conversationId = conversationId;
        this.response_mode = "streaming";
        this.inputs = new HashMap<>();  // 初始化为空 Map
    }

    // getters and setters for all fields...
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getResponse_mode() { return response_mode; }
    public void setResponse_mode(String response_mode) { this.response_mode = response_mode; }
    public Map<String, Object> getInputs() { return inputs; }
    public void setInputs(Map<String, Object> inputs) { this.inputs = inputs; }
}