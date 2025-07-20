package com.yanzhiyu.springai.mq;

import com.yanzhiyu.springai.entity.dto.ChatTypeDTO;
import com.yanzhiyu.springai.entity.dto.MsgDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * @author yanzhiyu
 * @date 2025/7/19
 */
@Service
@RequiredArgsConstructor
public class MsgProducerService {
    private static final String MSG_TOPIC = "msg-topic";

    private static final String CHAT_TYPE_TOPIC = "chat-type-topic";

    private final KafkaTemplate<String, MsgDTO> msgKafkaTemplate;

    private final KafkaTemplate<String, ChatTypeDTO> chatTypeKafkaTemplate;

    public void sendMessage(MsgDTO msgDTO) {
        msgKafkaTemplate.send(MSG_TOPIC, msgDTO);
        System.out.println("Produced message: " + msgDTO);
    }

    public void sendChatType(ChatTypeDTO chatTypeDTO) {
        chatTypeKafkaTemplate.send(CHAT_TYPE_TOPIC, chatTypeDTO);
        System.out.println("Produced message: " + chatTypeDTO);
    }
}
