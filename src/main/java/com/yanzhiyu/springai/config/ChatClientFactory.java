package com.yanzhiyu.springai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

/**
 * @author yanzhiyu
 * @date 2025/7/4
 */
@Component
public class ChatClientFactory {

    private final ChatMemory chatMemory;

    public ChatClientFactory(ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
    }

    public ChatClient createChatClient(ChatModel model, String systemPrompt) {
        return ChatClient.builder(model)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }
}
