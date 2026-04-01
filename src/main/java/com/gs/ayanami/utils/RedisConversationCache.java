package com.gs.ayanami.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class RedisConversationCache {

    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "conversation:temp:";

    public RedisConversationCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 初始化临时对话缓存
    public void init(String conversationId, String userId, String query) {
        String key = KEY_PREFIX + conversationId;
        Map<String, String> map = new HashMap<>();
        map.put("user_id", userId);
        map.put("query", query);
        map.put("answer", "");
        map.put("created_at", String.valueOf(System.currentTimeMillis()));
        map.put("updated_at", String.valueOf(System.currentTimeMillis()));
        redisTemplate.opsForHash().putAll(key, map);
        redisTemplate.expire(key, 30, TimeUnit.MINUTES); // 30分钟过期
    }

    // 追加 answer 片段
    public void appendAnswer(String conversationId, String chunk) {
        String key = KEY_PREFIX + conversationId;
        String current = (String) redisTemplate.opsForHash().get(key, "answer");
        if (current == null) current = "";
        current += chunk;
        redisTemplate.opsForHash().put(key, "answer", current);
        redisTemplate.opsForHash().put(key, "updated_at", String.valueOf(System.currentTimeMillis()));
    }

    // 获取完整 answer
    public String getAnswer(String conversationId) {
        String key = KEY_PREFIX + conversationId;
        return (String) redisTemplate.opsForHash().get(key, "answer");
    }

    // 获取对话元数据（用于保存到数据库）
    public Map<Object, Object> getConversationData(String conversationId) {
        String key = KEY_PREFIX + conversationId;
        return redisTemplate.opsForHash().entries(key);
    }

    // 删除临时缓存
    public void delete(String conversationId) {
        String key = KEY_PREFIX + conversationId;
        redisTemplate.delete(key);
    }
}
