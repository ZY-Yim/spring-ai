package com.yanzhiyu.springai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.yanzhiyu.springai.constants.SystemConstants.GAME_SYSTEM_PROMPT;
import static com.yanzhiyu.springai.constants.SystemConstants.SYSTEM_PROMPT;

/**
 * @author yanzhiyu
 * @date 2025/7/4
 */
@Configuration
public class CommonConfiguration {

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder().maxMessages(10).build();
    }

    @Bean
    public ChatClient chatClient(OllamaChatModel model, ChatClientFactory chatClientFactory) {
        return chatClientFactory.createChatClient(model, SYSTEM_PROMPT);
    }

    @Bean
    public ChatClient gameChatClient(OpenAiChatModel model, ChatClientFactory chatClientFactory) {
        return chatClientFactory.createChatClient(model, GAME_SYSTEM_PROMPT);
    }

    /*
    @Bean
    public ChatClient chatClient(OllamaChatModel model, ChatMemory chatMemory) {
        return ChatClient
                .builder(model)
                .defaultSystem(SYSTEM_PROMPT)
                // 配置日志Advisor
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }


    @Bean
    public ChatClient gameChatClient(OpenAiChatModel model, ChatMemory chatMemory) {
        return ChatClient
                .builder(model)
                .defaultSystem(GAME_SYSTEM_PROMPT)
                // 配置日志Advisor
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }
    */
}
