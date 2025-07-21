package com.yanzhiyu.springai.config;

import com.yanzhiyu.springai.Tools.CourseTools;
import com.yanzhiyu.springai.model.AlibabaOpenAiChatModel;
import com.yanzhiyu.springai.model.MyRedisVectorStore;
import com.yanzhiyu.springai.repository.RedisChatMemory;
import io.micrometer.observation.ObservationRegistry;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatProperties;
import org.springframework.ai.model.openai.autoconfigure.OpenAiConnectionProperties;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import redis.clients.jedis.JedisPooled;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.yanzhiyu.springai.constants.SystemConstants.*;

/**
 * @author yanzhiyu
 * @date 2025/7/4
 */
@Configuration
public class CommonConfiguration {

    @Resource
    private CourseTools courseTools;

    // @Bean
    // public ChatMemory chatMemory() {
    //     return MessageWindowChatMemory.builder().maxMessages(10).build();
    // }

    /*
    // 还不能用工厂生成，每个client的模式不一样
    @Bean
    public ChatClient chatClient(OllamaChatModel model, ChatClientFactory chatClientFactory) {
        return chatClientFactory.createChatClient(model, SYSTEM_PROMPT);
    }

    @Bean
    public ChatClient gameChatClient(OpenAiChatModel model, ChatClientFactory chatClientFactory) {
        return chatClientFactory.createChatClient(model, GAME_SYSTEM_PROMPT);
    }

    @Bean
    public ChatClient chatClient(OpenAiChatModel model, ChatMemory chatMemory) {
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
    */


    // 改造，支持多模态
    @Bean
    public ChatClient chatClient(AlibabaOpenAiChatModel model, RedisChatMemory chatMemory) {
        return ChatClient
                .builder(model)
                .defaultOptions(ChatOptions.builder().model("qwen-omni-turbo").build())
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


    // 1.0.0-M6 存在不兼容问题，1.0.0 已经解决
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

    @Bean
    public ChatClient pdfChatClient(OpenAiChatModel model, ChatMemory chatMemory, VectorStore redisVectorStore) {
        return ChatClient.builder(model)
                .defaultSystem("请根据上下文回答问题，遇到上下文没有的问题，不要随意编造。")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(redisVectorStore)
                                .searchRequest(
                                        SearchRequest.builder()
                                                .topK(2)
                                                .similarityThreshold(0.8)
                                                .build()
                                ).build())
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

    // 不加的话会有两个EmbeddingModel Bean，可以通过禁用ollama解决
    // 要新增MetadataFields，必须通过自定义Bean
    @Bean
    public MyRedisVectorStore redisVectorStore(OpenAiEmbeddingModel model, JedisPooled jedisPooled) {
        return MyRedisVectorStore.builder(jedisPooled, model)
                .prefix("doc:")
                .initializeSchema(true)
                .indexName("spring_ai_redis")
                .metadataFields(
                        // 定义为 TAG 类型
                        MyRedisVectorStore.MetadataField.tag("file_name"),
                        // 其他需要的字段...
                        MyRedisVectorStore.MetadataField.tag("unique_file_name"),
                        MyRedisVectorStore.MetadataField.tag("encode_file_name")
                )
                .build();
    }

    @Bean
    public SimpleVectorStore simpleVectorStore(OpenAiEmbeddingModel model) {
        return SimpleVectorStore.builder(model)
                .build();
    }


    @Bean
    public AlibabaOpenAiChatModel alibabaOpenAiChatModel(OpenAiConnectionProperties commonProperties, OpenAiChatProperties chatProperties, ObjectProvider<RestClient.Builder> restClientBuilderProvider, ObjectProvider<WebClient.Builder> webClientBuilderProvider, ToolCallingManager toolCallingManager, RetryTemplate retryTemplate, ResponseErrorHandler responseErrorHandler, ObjectProvider<ObservationRegistry> observationRegistry, ObjectProvider<ChatModelObservationConvention> observationConvention) {
        String baseUrl = StringUtils.hasText(chatProperties.getBaseUrl()) ? chatProperties.getBaseUrl() : commonProperties.getBaseUrl();
        String apiKey = StringUtils.hasText(chatProperties.getApiKey()) ? chatProperties.getApiKey() : commonProperties.getApiKey();
        String projectId = StringUtils.hasText(chatProperties.getProjectId()) ? chatProperties.getProjectId() : commonProperties.getProjectId();
        String organizationId = StringUtils.hasText(chatProperties.getOrganizationId()) ? chatProperties.getOrganizationId() : commonProperties.getOrganizationId();
        Map<String, List<String>> connectionHeaders = new HashMap<>();
        if (StringUtils.hasText(projectId)) {
            connectionHeaders.put("OpenAI-Project", List.of(projectId));
        }

        if (StringUtils.hasText(organizationId)) {
            connectionHeaders.put("OpenAI-Organization", List.of(organizationId));
        }
        RestClient.Builder restClientBuilder = restClientBuilderProvider.getIfAvailable(RestClient::builder);
        WebClient.Builder webClientBuilder = webClientBuilderProvider.getIfAvailable(WebClient::builder);
        OpenAiApi openAiApi = OpenAiApi.builder().baseUrl(baseUrl).apiKey(new SimpleApiKey(apiKey)).headers(CollectionUtils.toMultiValueMap(connectionHeaders)).completionsPath(chatProperties.getCompletionsPath()).embeddingsPath("/v1/embeddings").restClientBuilder(restClientBuilder).webClientBuilder(webClientBuilder).responseErrorHandler(responseErrorHandler).build();
        AlibabaOpenAiChatModel chatModel = AlibabaOpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(chatProperties.getOptions()).toolCallingManager(toolCallingManager).retryTemplate(retryTemplate).observationRegistry((ObservationRegistry)observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP)).build();
        Objects.requireNonNull(chatModel);
        observationConvention.ifAvailable(chatModel::setObservationConvention);
        return chatModel;
    }
}
