package com.yanzhiyu.springai.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.yanzhiyu.springai.properties.OssProperties;
import com.yanzhiyu.springai.utils.OssUtil;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yanzhiyu
 * @date 2025/7/17
 */
@Data
@Configuration
@Slf4j
public class OssConfiguration {

    private OSS ossClient;

    @Bean
    public OssUtil ossUtil(OSS ossClient, OssProperties ossProperties) {
        return new OssUtil(ossProperties.getEndpoint(), ossProperties.getBucketName(), ossProperties.getFileHost(), ossClient);
    }

    @Bean
    public OSS ossClient(OssProperties ossProperties) {
        return new OSSClientBuilder()
                .build(ossProperties.getEndpoint(), ossProperties.getAccessKeyId(), ossProperties.getAccessKeySecret());
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
}
