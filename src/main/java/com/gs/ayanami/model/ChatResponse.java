package com.gs.ayanami.model;

public class ChatResponse {
    private String message_id;
    private String conversation_id;
    private String answer;           // Dify返回的原始文本（JSON字符串）
    private TripPlan plan;            // 解析后的结构化行程

    // 原有getter/setter
    public String getMessage_id() { return message_id; }
    public void setMessage_id(String message_id) { this.message_id = message_id; }
    public String getConversation_id() { return conversation_id; }
    public void setConversation_id(String conversation_id) { this.conversation_id = conversation_id; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    // 新增
    public TripPlan getPlan() { return plan; }
    public void setPlan(TripPlan plan) { this.plan = plan; }
}