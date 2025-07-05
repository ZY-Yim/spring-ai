package com.yanzhiyu.springai.controller;

import com.yanzhiyu.springai.repository.ChatHistoryRepository;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

/**
 * @author yanzhiyu
 * @date 2025/7/4
 */
// 有参数构造器
// @RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class ChatController {

    @Resource
    ChatClient chatClient;

    @Resource
    ChatHistoryRepository chatHistoryRepository;

    @RequestMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public Flux<String> chat(String prompt, String chatId) {
        // 保存会话id
        chatHistoryRepository.save("chat", chatId);
        // 请求模型
        return chatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CONVERSATION_ID, chatId))
                .stream()
                .content();
    }
}
