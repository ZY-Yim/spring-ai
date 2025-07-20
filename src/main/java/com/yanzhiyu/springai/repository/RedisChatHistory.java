package com.yanzhiyu.springai.repository;

import com.yanzhiyu.springai.entity.dto.ChatTypeDTO;
import com.yanzhiyu.springai.mapper.ChatTypeMapper;
import com.yanzhiyu.springai.mapper.MsgMapper;
import com.yanzhiyu.springai.mq.MsgProducerService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author yanzhiyu
 * @date 2025/7/15
 */
@Component
public class RedisChatHistory implements ChatHistoryRepository {

    private static final String CHAT_HISTORY_KEY_PREFIX = "chat:history:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private MsgProducerService msgProducerService;

    @Resource
    private ChatTypeMapper chatTypeMapper;

    @Override
    public void save(String type, String chatId) {
        stringRedisTemplate.opsForSet().add(CHAT_HISTORY_KEY_PREFIX + type, chatId);
        stringRedisTemplate.expire(CHAT_HISTORY_KEY_PREFIX + type, 30, TimeUnit.MINUTES);

        // 写入消息队列
        ChatTypeDTO chatTypeDTO = new ChatTypeDTO();
        chatTypeDTO.setCreateTime(LocalDateTime.now());
        chatTypeDTO.setChatType(type);
        chatTypeDTO.setChatId(chatId);
        msgProducerService.sendChatType(chatTypeDTO);
    }

    @Override
    public List<String> getChatIds(String type) {
        if (stringRedisTemplate.hasKey(CHAT_HISTORY_KEY_PREFIX + type)) {
            Set<String> chatIds = stringRedisTemplate.opsForSet().members(CHAT_HISTORY_KEY_PREFIX + type);
            if (chatIds == null || chatIds.isEmpty()) {
                return Collections.emptyList();
            }
            return chatIds.stream().sorted(String::compareTo).toList();
        }

        // 查db
        List<ChatTypeDTO> chatTypeDTOList = chatTypeMapper.getChatIds(type);
        List<String> list = chatTypeDTOList.stream().map(ChatTypeDTO::getChatId).distinct().toList();

        // 写入redis
        stringRedisTemplate.opsForSet().add(CHAT_HISTORY_KEY_PREFIX + type, list.toArray(new String[0]));
        return list;
    }
}
