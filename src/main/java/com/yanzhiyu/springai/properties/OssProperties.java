package com.yanzhiyu.springai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author yanzhiyu
 * @date 2025/7/17
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.oss")
public class OssProperties {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String fileHost;
}
