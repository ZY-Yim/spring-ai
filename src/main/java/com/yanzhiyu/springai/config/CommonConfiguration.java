package com.yanzhiyu.springai.config;

import com.yanzhiyu.springai.Tools.CourseTools;
import jakarta.annotation.Resource;
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

import static com.yanzhiyu.springai.constants.SystemConstants.*;

/**
 * @author yanzhiyu
 * @date 2025/7/4
 */
@Configuration
public class CommonConfiguration {

    @Resource
    private CourseTools courseTools;

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

    // 还不能用工厂生成，每个client的模式不一样
    @Bean
    public ChatClient serviceChatClient(OpenAiChatModel model, ChatMemory chatMemory) {
        return ChatClient.builder(model)
                .defaultSystem(SERVICE_SYSTEM_PROMPT)
                // 配置日志Advisor
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .defaultTools(courseTools)
                .build();
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
