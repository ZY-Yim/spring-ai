package com.yanzhiyu.springai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author yanzhiyu
 * @date 2025/7/4
 */
@Configuration
public class MvcConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许哪些路径
        registry.addMapping("/**")
                // 允许哪些域来访问
                .allowedOrigins("*")
                // 允许哪些请求方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许哪些请求头
                .allowedHeaders("*")
                ;
    }
}
