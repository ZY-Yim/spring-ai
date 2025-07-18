// package com.yanzhiyu.springai.repository;
//
// import jakarta.annotation.PostConstruct;
// import jakarta.annotation.PreDestroy;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.ai.vectorstore.SimpleVectorStore;
// import org.springframework.ai.vectorstore.redis.RedisVectorStore;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.core.io.FileSystemResource;
// import org.springframework.core.io.Resource;
// import org.springframework.stereotype.Component;
//
// import java.io.*;
// import java.nio.charset.StandardCharsets;
// import java.nio.file.Files;
// import java.time.LocalDateTime;
// import java.util.Objects;
// import java.util.Properties;
//
// /**
//  * @author yanzhiyu
//  * @date 2025/7/6
//  */
// @Slf4j
// @Component
// // @RequiredArgsConstructor
// public class LocalPdfFileRepository implements FileRepository {
//
//     @jakarta.annotation.Resource
//     SimpleVectorStore simpleVectorStore;
//
//     @jakarta.annotation.Resource
//     RedisVectorStore redisVectorStore;
//
//     // 会话id 与 文件名的对应关系，方便查询会话历史时重新加载文件
//     // 自带持久化存储能力，重启还在
//     private final Properties chatFiles = new Properties();
//
//     @Override
//     public boolean save(String chatId, Resource resource) {
//         // 2.保存到本地磁盘
//         String filename = resource.getFilename();
//         // 相对路径，当前项目目录
//         File target = new File(Objects.requireNonNull(filename));
//         if (!target.exists()) {
//             try {
//                 Files.copy(resource.getInputStream(), target.toPath());
//             } catch (IOException e) {
//                 log.error("Failed to save PDF resource.", e);
//                 return false;
//             }
//         }
//         // 3.保存映射关系
//         chatFiles.put(chatId, filename);
//         return true;
//     }
//
//     @Override
//     public Resource getFile(String chatId) {
//         return new FileSystemResource(chatFiles.getProperty(chatId));
//     }
//
//     // @PostConstruct
//     // private void init() {
//     //     FileSystemResource pdfResource = new FileSystemResource("chat-pdf.properties");
//     //     if (pdfResource.exists()) {
//     //         try {
//     //             chatFiles.load(new BufferedReader(new InputStreamReader(pdfResource.getInputStream(), StandardCharsets.UTF_8)));
//     //         } catch (IOException e) {
//     //             throw new RuntimeException(e);
//     //         }
//     //     }
//     //     FileSystemResource vectorResource = new FileSystemResource("chat-pdf.json");
//     //     if (vectorResource.exists()) {
//     //         simpleVectorStore.load(vectorResource);
//     //     }
//     // }
//     //
//     // @PreDestroy
//     // private void persistent() {
//     //     try {
//     //         chatFiles.store(new FileWriter("chat-pdf.properties"), LocalDateTime.now().toString());
//     //         simpleVectorStore.save(new File("chat-pdf.json"));
//     //     } catch (IOException e) {
//     //         throw new RuntimeException(e);
//     //     }
//     // }
// }
