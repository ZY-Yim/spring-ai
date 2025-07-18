package com.yanzhiyu.springai.repository;

import com.aliyun.oss.OSS;
import com.yanzhiyu.springai.utils.OssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.UUID;

/**
 * @author yanzhiyu
 * @date 2025/7/17
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OssPdfFileRepository implements FileRepository{

    @Autowired
    private OssUtil ossUtil;

    private final StringRedisTemplate redisTemplate;

    private final static String CHAT_FILE_KEY_PREFIX = "chat:pdf:";

    @Override
    public String save(String chatId, Resource resource) {
        try {
            // ✅ 修复点：使用 InputStream 读取资源，而不是 getFile()
            byte[] fileBytes = FileCopyUtils.copyToByteArray(resource.getInputStream());

            String fileName = resource.getFilename();
            // 使用 chatId + UUID + 原始文件名，确保唯一
            String uniqueFilename = chatId + "_" + UUID.randomUUID().toString().replace("-", "") + "_" + fileName;
            String encodeFilename = URLEncoder.encode(Objects.requireNonNull(uniqueFilename), StandardCharsets.UTF_8).replace("+", "%20");

            // 上传的文件名也保持唯一
            String fileUrl = ossUtil.upload(fileBytes, encodeFilename);

            // 使用 Redis Hash 存储原始文件名和编码后的文件名
            String redisKey = CHAT_FILE_KEY_PREFIX + chatId;
            redisTemplate.opsForHash().put(redisKey, "uniqueFilename", uniqueFilename);
            redisTemplate.opsForHash().put(redisKey, "encodeFileName", encodeFilename);

            return uniqueFilename;
        } catch (IOException e) {
            log.error("Failed to upload PDF to OSS.", e);
            return "";
        }
    }

    @Override
    public Resource getFile(String chatId) {
        String encodeFileName = getEncodeFileName(chatId);

        if (encodeFileName == null) {
            throw new RuntimeException("File not found for chatId: " + chatId);
        }

        byte[] fileBytes = ossUtil.download(encodeFileName);
        return new InputStreamResource(new ByteArrayInputStream(fileBytes));
    }

    @Override
    public String getEncodeFileName(String chatId) {
        String redisKey = CHAT_FILE_KEY_PREFIX + chatId;
        Object encodedFilename = redisTemplate.opsForHash().get(redisKey, "encodeFileName");
        if (encodedFilename == null) {
            throw new RuntimeException("File not found for chatId: " + chatId);
        }
        return encodedFilename.toString();
    }

    @Override
    public String getUniqueFileName(String chatId) {
        String redisKey = CHAT_FILE_KEY_PREFIX + chatId;
        Object encodedFilename = redisTemplate.opsForHash().get(redisKey, "uniqueFilename");
        if (encodedFilename == null) {
            throw new RuntimeException("File not found for chatId: " + chatId);
        }
        return encodedFilename.toString();
    }
}
