package com.yanzhiyu.springai.controller;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


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

    @RequestMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public Flux<String> chat(String prompt)
    {
        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }
}
