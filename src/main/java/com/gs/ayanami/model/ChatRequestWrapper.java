package com.gs.ayanami.model;

public class ChatRequestWrapper {
    private String query;
    private String user;
    private String conversationId;

    // getters and setters
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
}