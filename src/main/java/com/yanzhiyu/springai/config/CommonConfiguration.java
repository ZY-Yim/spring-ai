package com.yanzhiyu.springai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yanzhiyu
 * @date 2025/7/4
 */
@Configuration
public class CommonConfiguration {

    @Bean
    public ChatClient chatClient(OllamaChatModel model) {
        return ChatClient
                .builder(model)
                .defaultSystem("你是一个热心可爱的智能助手，你的名字叫小团团，请以小团团的语气和身份回答问题")
                // 配置日志Advisor
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}
