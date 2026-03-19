package com.gs.ayanami.model;

public class ChatResponse {
    private String message_id;
    private String conversation_id;
    private String answer;          // 保留原始JSON（可选）
    private MapData plan;            // 地图数据
    private String richText;         // 富文本计划

    // 原有 getter/setter
    public String getMessage_id() { return message_id; }
    public void setMessage_id(String message_id) { this.message_id = message_id; }
    public String getConversation_id() { return conversation_id; }
    public void setConversation_id(String conversation_id) { this.conversation_id = conversation_id; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    // 新增
    public MapData getPlan() { return plan; }
    public void setPlan(MapData plan) { this.plan = plan; }
    public String getRichText() { return richText; }
    public void setRichText(String richText) { this.richText = richText; }
}