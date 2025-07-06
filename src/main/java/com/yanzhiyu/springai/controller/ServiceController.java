package com.yanzhiyu.springai.controller;

import com.yanzhiyu.springai.repository.ChatHistoryRepository;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

/**
 * @author yanzhiyu
 * @date 2025/7/6
 */
@RestController
@RequestMapping("/ai")
public class ServiceController {
    @Resource
    private ChatClient serviceChatClient;

    @Resource
    private ChatHistoryRepository chatHistoryRepository;

    // 流式调用不兼容
    @RequestMapping(value = "/service", produces = "text/html;charset=utf-8")
    public String service(String prompt, String chatId) {
        // 保存会话id
        chatHistoryRepository.save("service", chatId);
        // 请求模型
        return serviceChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CONVERSATION_ID, chatId))
                .call()
                .content();
    }

}
