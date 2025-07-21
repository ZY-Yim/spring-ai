package com.yanzhiyu.springai.controller;

import com.yanzhiyu.springai.entity.vo.Result;
import com.yanzhiyu.springai.repository.ChatHistoryRepository;
import com.yanzhiyu.springai.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor.FILTER_EXPRESSION;
import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

/**
 * @author yanzhiyu
 * @date 2025/7/6
 */
@Slf4j
@RestController
@RequestMapping("/ai/pdf")
public class PdfController {

    @jakarta.annotation.Resource
    FileRepository fileRepository;

    @jakarta.annotation.Resource
    SimpleVectorStore simpleVectorStore;

    @jakarta.annotation.Resource
    RedisVectorStore redisVectorStore;

    @jakarta.annotation.Resource
    ChatClient pdfChatClient;

    @jakarta.annotation.Resource
    ChatHistoryRepository chatHistoryRepository;


    @RequestMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public Flux<String> chat(String prompt, String chatId) {
        // 找到会话文件名
        // InputStreamResource 类型，它默认不会携带文件名信息
        // Resource file = fileRepository.getFile(chatId);
        String uniqueFileName = fileRepository.getUniqueFileName(chatId);
        String encodeFileName = fileRepository.getEncodeFileName(chatId);
        if (uniqueFileName.isEmpty()) {
            return Flux.just("请上传PDF文件！");
        }
        // 保存会话id，上传文件的时候已经写入了，这里不需要再写入了
        // chatHistoryRepository.save("pdf", chatId);
        // 请求模型
        return pdfChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CONVERSATION_ID, chatId))
                .advisors(a -> a.param(FILTER_EXPRESSION, "encode_file_name == '" +
                        encodeFileName.replace(".", "\\.")
                                .replace("%", "\\%") + "'")
                )
                .stream()
                .content();
    }

    /**
     * 文件上传
     */
    @RequestMapping("/upload/{chatId}")
    public Result uploadPdf(@PathVariable String chatId, @RequestParam("file") MultipartFile file) {
        try {
            // 1. 校验文件是否为PDF格式
            if (!Objects.equals(file.getContentType(), "application/pdf")) {
                return Result.fail("只能上传PDF文件！");
            }
            // 2.保存文件
            fileRepository.save(chatId, file.getResource());
            String uniqueFileName = fileRepository.getUniqueFileName(chatId);
            String encodeFileName = fileRepository.getEncodeFileName(chatId);
            if (uniqueFileName.isEmpty()) {
                return Result.fail("保存文件失败！");
            }
            // 3.写入向量库
            this.writeToVectorStore(file.getResource(), uniqueFileName, encodeFileName);
            return Result.ok();
        } catch (Exception e) {
            log.error("Failed to upload PDF.", e);
            return Result.fail("上传文件失败！");
        }
    }

    /**
     * 文件下载
     */
    @GetMapping("/file/{chatId}")
    public ResponseEntity<Resource> download(@PathVariable("chatId") String chatId) throws IOException {
        // 1.读取文件
        Resource resource = fileRepository.getFile(chatId);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        // 2.文件名编码，写入响应头
        // save的时候已经编码了
        String encodeFileName = fileRepository.getEncodeFileName(chatId);
        // String encodeFileName = URLEncoder.encode(Objects.requireNonNull(fileName), StandardCharsets.UTF_8);
        // 3.返回文件
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + encodeFileName + "\"")
                .body(resource);
    }

    private void writeToVectorStore(Resource resource, String uniqueFileName, String encodeFileName) {
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
        redisVectorStore.add(documents);
    }
}