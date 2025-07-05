package com.yanzhiyu.springai.controller;

import com.yanzhiyu.springai.entity.vo.MessageVO;
import com.yanzhiyu.springai.repository.ChatHistoryRepository;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author yanzhiyu
 * @date 2025/7/5
 */
@RestController
@RequestMapping("/ai/history")
public class ChatHistoryController {

    @Resource
    ChatHistoryRepository chatHistoryRepository;

    @Resource
    ChatMemory chatMemory;

    @RequestMapping("/{type}")
    public List<String> getChatIds(@PathVariable("type") String type) {
        return chatHistoryRepository.getChatIds(type);
    }

    @RequestMapping("/{type}/{chatId}")
    public List<MessageVO> getHistory(@PathVariable("type") String type, @PathVariable("chatId") String chatId) {
        List<Message> messages = chatMemory.get(chatId);
        if (messages == null) {
            return List.of();
        }
        return messages.stream().map(MessageVO::new).toList();
    }
}
