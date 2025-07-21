package com.yanzhiyu.springai.repository;

import com.yanzhiyu.springai.entity.dto.PdfFileDTO;
import com.yanzhiyu.springai.mapper.PdfFileMapper;
import com.yanzhiyu.springai.model.MyRedisVectorStore;
import com.yanzhiyu.springai.mq.MsgProducerService;
import com.yanzhiyu.springai.utils.OssUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.filter.Filter;
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
import java.util.List;
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

    @jakarta.annotation.Resource
    MyRedisVectorStore myRedisVectorStore;

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
            // 上传oss
            String fileUrl = ossUtil.upload(fileBytes, encodeFilename);

            // 会话记录写入到Redis
            redisChatHistory.save("pdf", chatId);

            // 使用 Redis Hash 存储原始文件名和编码后的文件名
            saveToRedis(chatId, uniqueFilename, encodeFilename);

            // 写入向量数据库
            writeToVectorStore(resource, uniqueFilename, encodeFilename);

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

    /**
     * 获取文件编码后的文件名
     * 每次chat/下载文件的时候会调用到，需要设置chatId的过期时间和Document的过期时间
     *
     * @param chatId 会话id
     * @return 文件编码后的文件名
     */
    @Override
    public String getEncodeFileName(String chatId) {
        String redisKey = CHAT_FILE_KEY_PREFIX + chatId;
        if (stringRedisTemplate.hasKey(redisKey)) {
            Object encodedFilename = stringRedisTemplate.opsForHash().get(redisKey, "encodeFileName");
            if (encodedFilename == null) {
                throw new RuntimeException("File not found for chatId: " + chatId);
            }
            setExpire(chatId);
            setDocumentExpire(encodedFilename.toString());

            return encodedFilename.toString();
        }

        // 从db获取
        PdfFileDTO pdfFile = pdfFileMapper.getPdfFile(chatId);
        if (pdfFile == null) {
            throw new RuntimeException("File not found for chatId: " + chatId);
        }
        saveToRedis(chatId, pdfFile.getUniqueFileName(), pdfFile.getEncodeFileName());
        setDocumentExpire(pdfFile.getEncodeFileName());

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
        saveToRedis(chatId, pdfFile.getUniqueFileName(), pdfFile.getEncodeFileName());

        return pdfFile.getUniqueFileName();
    }

    @Override
    public void writeToVectorStore(Resource resource, String uniqueFileName, String encodeFileName) {
        // 1.创建PDF的读取器
        PagePdfDocumentReader reader = new PagePdfDocumentReader(
                // 文件源
                resource,
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                        // 每1页PDF作为一个Document
                        .withPagesPerDocument(1)
                        .build()
        );

        // 2.读取PDF文档，拆分为Document
        List<Document> documents = reader.read();
        documents.forEach(doc -> doc.getMetadata().put("unique_file_name", uniqueFileName));
        documents.forEach(doc -> doc.getMetadata().put("encode_file_name", encodeFileName));

        // 3.写入向量库
        myRedisVectorStore.add(documents);
    }


    public void setExpire(String chatId) {
        // 设置会话文件信息记录的过期时间
        stringRedisTemplate.expire(CHAT_FILE_KEY_PREFIX + chatId, 30, TimeUnit.MINUTES);
    }

    public void saveToRedis(String chatId, String uniqueFileName, String encodeFileName) {
        String redisKey = CHAT_FILE_KEY_PREFIX + chatId;
        stringRedisTemplate.opsForHash().put(redisKey, "uniqueFilename", uniqueFileName);
        stringRedisTemplate.opsForHash().put(redisKey, "encodeFileName", encodeFileName);
        setExpire(chatId);
    }

    public void setDocumentExpire(String encodedFilename) {
        // 判断redis中是否有数据，有的话设置过期时间，没有的话从db加载
        if (myRedisVectorStore.doExpire(new Filter.Expression(Filter.ExpressionType.EQ
                , new Filter.Key("encode_file_ame")
                , new Filter.Value(encodedFilename.replace(".", "\\.").replace("%", "\\%"))))) {
            return;
        }

        // 从db加载
        return;
    }
}
