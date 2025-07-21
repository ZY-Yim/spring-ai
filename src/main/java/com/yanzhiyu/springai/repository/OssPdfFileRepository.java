package com.yanzhiyu.springai.repository;

import com.yanzhiyu.springai.entity.dto.PdfFileDTO;
import com.yanzhiyu.springai.mapper.PdfFileMapper;
import com.yanzhiyu.springai.mq.MsgProducerService;
import com.yanzhiyu.springai.utils.OssUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author yanzhiyu
 * @date 2025/7/17
 */
@Slf4j
@Component
public class OssPdfFileRepository implements FileRepository {

    private static final String CHAT_FILE_KEY_PREFIX = "chat:pdf:";

    @jakarta.annotation.Resource
    private OssUtil ossUtil;

    @jakarta.annotation.Resource
    private StringRedisTemplate stringRedisTemplate;

    @jakarta.annotation.Resource
    private PdfFileMapper pdfFileMapper;

    @jakarta.annotation.Resource
    private MsgProducerService msgProducerService;

    @jakarta.annotation.Resource
    RedisChatHistory redisChatHistory;

    @Override
    public Boolean save(String chatId, Resource resource) {
        try {
            // ✅ 修复点：使用 InputStream 读取资源，而不是 getFile()
            byte[] fileBytes = FileCopyUtils.copyToByteArray(resource.getInputStream());

            String fileName = Objects.requireNonNull(resource.getFilename()).replace("-", "_");
            // 使用 chatId + UUID + 原始文件名，确保唯一
            String uniqueFilename = chatId + "_" + UUID.randomUUID().toString().replace("-", "") + "_" + fileName;
            String encodeFilename = URLEncoder.encode(Objects.requireNonNull(uniqueFilename), StandardCharsets.UTF_8).replace("+", "%20");

            // 上传的文件名也保持唯一
            String fileUrl = ossUtil.upload(fileBytes, encodeFilename);

            // 会话记录写入到Redis
            redisChatHistory.save("pdf", chatId);

            // 使用 Redis Hash 存储原始文件名和编码后的文件名
            String redisKey = CHAT_FILE_KEY_PREFIX + chatId;
            saveToRedis(redisKey, uniqueFilename, encodeFilename);

            // 异步写入消息队列
            PdfFileDTO pdfFileDTO = new PdfFileDTO();
            pdfFileDTO.setCreateTime(LocalDateTime.now());
            pdfFileDTO.setUpdateTime(LocalDateTime.now());
            pdfFileDTO.setChatId(chatId);
            pdfFileDTO.setUniqueFileName(uniqueFilename);
            pdfFileDTO.setEncodeFileName(encodeFilename);
            msgProducerService.sendPdfFile(pdfFileDTO);

            return true;
        } catch (IOException e) {
            log.error("Failed to upload PDF to OSS.", e);
            return false;
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
        if (stringRedisTemplate.hasKey(redisKey)) {
            Object encodedFilename = stringRedisTemplate.opsForHash().get(redisKey, "encodeFileName");
            if (encodedFilename == null) {
                throw new RuntimeException("File not found for chatId: " + chatId);
            }
            return encodedFilename.toString();
        }

        // 从db获取
        PdfFileDTO pdfFile = pdfFileMapper.getPdfFile(chatId);
        if (pdfFile == null) {
            throw new RuntimeException("File not found for chatId: " + chatId);
        }
        saveToRedis(redisKey, pdfFile.getUniqueFileName(), pdfFile.getEncodeFileName());

        return pdfFile.getEncodeFileName();
    }

    @Override
    public String getUniqueFileName(String chatId) {
        String redisKey = CHAT_FILE_KEY_PREFIX + chatId;
        if (stringRedisTemplate.hasKey(redisKey)) {
            Object encodedFilename = stringRedisTemplate.opsForHash().get(redisKey, "uniqueFilename");
            if (encodedFilename == null) {
                throw new RuntimeException("File not found for chatId: " + chatId);
            }
            return encodedFilename.toString();
        }

        // 从db获取
        PdfFileDTO pdfFile = pdfFileMapper.getPdfFile(chatId);
        if (pdfFile == null) {
            throw new RuntimeException("File not found for chatId: " + chatId);
        }
        saveToRedis(redisKey, pdfFile.getUniqueFileName(), pdfFile.getEncodeFileName());

        return pdfFile.getUniqueFileName();
    }


    public void saveToRedis(String redisKey, String uniqueFileName, String encodeFileName){
        stringRedisTemplate.opsForHash().put(redisKey, "uniqueFilename", uniqueFileName);
        stringRedisTemplate.opsForHash().put(redisKey, "encodeFileName", encodeFileName);
        stringRedisTemplate.expire(redisKey, 1, TimeUnit.HOURS);
    }

}
