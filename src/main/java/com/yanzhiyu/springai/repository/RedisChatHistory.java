package com.yanzhiyu.springai.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author yanzhiyu
 * @date 2025/7/15
 */
@RequiredArgsConstructor
@Component
public class RedisChatHistory implements ChatHistoryRepository{

    private final StringRedisTemplate redisTemplate;

    private final static String CHAT_HISTORY_KEY_PREFIX = "chat:history:";

    @Override
    public void save(String type, String chatId) {
        redisTemplate.opsForSet().add(CHAT_HISTORY_KEY_PREFIX + type, chatId);
    }

    @Override
    public List<String> getChatIds(String type) {
        Set<String> chatIds = redisTemplate.opsForSet().members(CHAT_HISTORY_KEY_PREFIX + type);
        if(chatIds == null || chatIds.isEmpty()) {
            return Collections.emptyList();
        }
        return chatIds.stream().sorted(String::compareTo).toList();
    }
}
