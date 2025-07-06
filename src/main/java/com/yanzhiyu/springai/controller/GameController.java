package com.yanzhiyu.springai.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

/**
 * @author yanzhiyu
 * @date 2025/7/5
 */
@RestController
@RequestMapping("/ai")
public class GameController {

    @Resource
    ChatClient gameChatClient;

    @RequestMapping(value = "/game")
    public Flux<String> chat(String prompt, String chatId) {
        // 请求模型
        return gameChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CONVERSATION_ID, chatId))
                .stream()
                .content();
    }
}
