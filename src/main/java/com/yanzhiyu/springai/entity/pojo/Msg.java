package com.yanzhiyu.springai.entity.pojo;

import lombok.Data;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.Message;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author yanzhiyu
 * @date 2025/7/14
 */
@Data
public class Msg implements Serializable {
    MessageType messageType;
    String text;
    Map<String, Object> metadata;

    public Msg() {
    }

    public Msg(Message message) {
        this.messageType = message.getMessageType();
        this.text = message.getText();
        this.metadata = message.getMetadata();
    }

    public Message toMessage() {
        return switch (messageType) {
            case SYSTEM -> new SystemMessage(text);
            case USER -> UserMessage.builder().text(text).media(List.of()).metadata(metadata).build();
            case ASSISTANT -> new AssistantMessage(text, metadata, List.of(), List.of());
            default -> throw new IllegalArgumentException("Unsupported message type: " + messageType);
        };
    }
}