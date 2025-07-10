package com.yanzhiyu.springai.controller;

import com.yanzhiyu.springai.repository.ChatHistoryRepository;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

/**
 * @author yanzhiyu
 * @date 2025/7/4
 */
// 有参数构造器
// @RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class ChatController {

    @Resource
    ChatClient chatClient;

    @Resource
    ChatHistoryRepository chatHistoryRepository;

    @RequestMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestParam("prompt") String prompt,
                             @RequestParam("chatId") String chatId,
                             @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        // 保存会话id
        chatHistoryRepository.save("chat", chatId);
        if (files == null || files.isEmpty()) {
            return textChat(prompt, chatId);
        } else {
            return multiModalChat(prompt, chatId, files);
        }


    }

    private Flux<String> multiModalChat(String prompt, String chatId, List<MultipartFile> files) {
        // 解析多媒体
        List<Media> medias = files.stream()
                .map(file -> new Media(
                                MimeType.valueOf(Objects.requireNonNull(file.getContentType())),
                                file.getResource()
                        )
                ).toList();

        return chatClient.prompt()
                .user(p -> p.text(prompt).media(medias.toArray(Media[]::new)))
                .advisors(a -> a.param(CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

    private Flux<String> textChat(String prompt, String chatId) {
        // 请求模型
        return chatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

    @RequestMapping(value = "/hello", produces = "text/html;charset=utf-8")
    public String chat() {
        return "hello world!";
    }
}
