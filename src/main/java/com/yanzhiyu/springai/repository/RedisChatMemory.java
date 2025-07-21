package com.yanzhiyu.springai.repository;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yanzhiyu.springai.entity.dto.MsgDTO;
import com.yanzhiyu.springai.mapper.MsgMapper;
import com.yanzhiyu.springai.mq.MsgProducerService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.yanzhiyu.springai.entity.pojo.Msg;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author yanzhiyu
 * @date 2025/7/15
 */
@Component
public class RedisChatMemory implements ChatMemory {

    private static final int DEFAULT_MAX_MESSAGES = 20;
    private static final String PREFIX = "chat:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private MsgProducerService msgProducerService;

    @Resource
    private MsgMapper msgMapper;

    /**
     * 添加消息
     *
     * @param conversationId 会话id
     * @param messages       消息
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
        stringRedisTemplate.opsForList().leftPushAll(PREFIX + conversationId, list);

        // 3. 只保留最近20条消息（截断列表）
        // 不应该这样做，这样会导致前端的聊天记录丢失
        // stringRedisTemplate.opsForList().trim(PREFIX + conversationId, 0, DEFAULT_MAX_MESSAGES - 1);

        // 设置过期时间半小时
        stringRedisTemplate.expire(PREFIX + conversationId, 30, TimeUnit.MINUTES);

        // 4. 发送到消息队列
        MsgDTO msgDTO = new MsgDTO();
        msgDTO.setChatId(conversationId);
        msgDTO.setCreateTime(LocalDateTime.now());
        msgDTO.setMessageType(messages.get(0).getMessageType().getValue());
        msgDTO.setText(messages.get(0).getText());
        msgDTO.setMetadata(JSON.toJSONString(messages.get(0).getMetadata()));
        msgProducerService.sendMessage(msgDTO);
    }

    /**
     * 获取指定范围的数据
     *
     * @param conversationId 会话id
     * @return 消息列表
     */
    @Override
    public List<Message> get(String conversationId) {
        List<String> list = stringRedisTemplate.opsForList().range(PREFIX + conversationId, 0, DEFAULT_MAX_MESSAGES);
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

    public List<Message> getAll(String conversationId) {
        // redis有缓存
        if (stringRedisTemplate.hasKey(PREFIX + conversationId)) {
            List<String> list = stringRedisTemplate.opsForList().range(PREFIX + conversationId, 0, -1);
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

        // 没有的话从db里获取，然后塞到redis 中
        List<MsgDTO> msgDTOList = msgMapper.getMsgList(conversationId);
        if (msgDTOList == null || msgDTOList.isEmpty()) {
            return List.of();
        }
        // 转为Message返回
        List<Message> messages = msgDTOList.stream().map(MsgDTO::toMessage).toList();
        // 转为redis格式
        List<String> list = messages.stream().map(Msg::new).map(msg -> {
            try {
                return objectMapper.writeValueAsString(msg);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).toList();
        // 写入redis
        stringRedisTemplate.opsForList().leftPushAll(PREFIX + conversationId, list);
        stringRedisTemplate.expire(PREFIX + conversationId, 30, TimeUnit.MINUTES);
        return messages;
    }

    /**
     * 清空指定会话的数据
     *
     * @param conversationId 会话id
     */
    @Override
    public void clear(String conversationId) {
        stringRedisTemplate.delete(PREFIX + conversationId);
    }
}
