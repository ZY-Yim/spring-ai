package com.yanzhiyu.springai.entity.dto;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.springframework.ai.chat.messages.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author yanzhiyu
 * @date 2025/7/19
 */
@Data
public class MsgDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private LocalDateTime createTime;

    private String chatId;

    private String messageType;

    private String text;

    private String metadata;

    public Message toMessage() {
        return switch (MessageType.fromValue(messageType)) {
            case SYSTEM -> new SystemMessage(text);
            case USER -> UserMessage.builder()
                    .text(text)
                    .media(List.of())
                    .metadata(JSON.parseObject(metadata, Map.class))
                    .build();
            case ASSISTANT -> new AssistantMessage(text,
                    JSON.parseObject(metadata, Map.class),
                    List.of(),
                    List.of());
            default -> throw new IllegalArgumentException("Unsupported message type: " + messageType);
        };
    }

}
