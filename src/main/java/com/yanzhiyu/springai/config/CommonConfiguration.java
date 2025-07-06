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
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStoreContent;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

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

    /*// 还不能用工厂生成，每个client的模式不一样
    @Bean
    public ChatClient chatClient(OllamaChatModel model, ChatClientFactory chatClientFactory) {
        return chatClientFactory.createChatClient(model, SYSTEM_PROMPT);
    }

    @Bean
    public ChatClient gameChatClient(OpenAiChatModel model, ChatClientFactory chatClientFactory) {
        return chatClientFactory.createChatClient(model, GAME_SYSTEM_PROMPT);
    }*/


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

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public JedisPooled jedisPooled() {
        return new JedisPooled(host, port);
    }

    // 不加点话会有两个EmbeddingModel Bean
    @Bean
    public RedisVectorStore vectorStore(OpenAiEmbeddingModel model, JedisPooled jedisPooled) {
        return RedisVectorStore.builder(jedisPooled, model)
                .prefix("doc:")
                .initializeSchema(true)
                .indexName("spring_ai_redis")
                .build();
    }

    @Bean
    public SimpleVectorStore simpleVectorStore(OpenAiEmbeddingModel model) {
        return SimpleVectorStore.builder(model)
                .build();
    }
}
