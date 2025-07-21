package com.yanzhiyu.springai.config;

import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author yanzhiyu
 * @date 2025/7/21
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "milvus")
public class MilvusConfiguration {

    private String host;
    private int port;
    private long connectTimeout;


    // @Bean
    // public MilvusClient milvusClient() {
    //     ConnectParam.Builder builder = ConnectParam.newBuilder()
    //             .withHost(host)
    //             .withPort(port)
    //             .withConnectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
    //
    //     return new MilvusServiceClient(builder.build());
    // }
}
