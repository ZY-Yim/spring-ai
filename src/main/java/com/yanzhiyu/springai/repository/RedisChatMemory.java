package com.yanzhiyu.springai.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.yanzhiyu.springai.entity.pojo.Msg;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author yanzhiyu
 * @date 2025/7/15
 */
@RequiredArgsConstructor
@Component
public class RedisChatMemory implements ChatMemory {

    private static final int DEFAULT_MAX_MESSAGES = 20;

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    private final static String PREFIX = "chat:";

    /**
     * 添加消息
     * @param conversationId 会话id
     * @param messages 消息
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        // 1. 将消息转换为 JSON 字符串
        List<String> list = messages.stream().map(Msg::new).map(msg -> {
            try {
                return objectMapper.writeValueAsString(msg);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).toList();

        // 2. 将新消息批量插入 Redis List 左侧（最新消息在前）
        redisTemplate.opsForList().leftPushAll(PREFIX + conversationId, list);

        // 3. 只保留最近20条消息（截断列表）
        redisTemplate.opsForList().trim(PREFIX + conversationId, 0, DEFAULT_MAX_MESSAGES - 1);

    }

    /**
     * 获取指定范围的数据
     * @param conversationId 会话id
     * @return 消息列表
     */
    @Override
    public List<Message> get(String conversationId) {
        List<String> list = redisTemplate.opsForList().range(PREFIX + conversationId, 0, DEFAULT_MAX_MESSAGES);
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return list.stream().map(s -> {
            try {
                return objectMapper.readValue(s, Msg.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).map(Msg::toMessage).toList();
    }

    /**
     * 清空指定会话的数据
     * @param conversationId 会话id
     */
    @Override
    public void clear(String conversationId) {
        redisTemplate.delete(PREFIX + conversationId);
    }

    public static void main(){
        String str = """
                {
                  "messageType" : "ASSISTANT",
                  "text" : "你好！很高兴能为你服务。有什么我可以帮助你的吗？",
                  "metadata" : {
                    "role" : "ASSISTANT",
                    "messageType" : "ASSISTANT",
                    "finishReason" : "STOP",
                    "refusal" : "",
                    "index" : 0,
                    "annotations" : [ ],
                    "id" : "chatcmpl-4ab65662-2b71-9e27-800a-b680766f9cfc"
                  }
                }
                """;
        
    }
}
