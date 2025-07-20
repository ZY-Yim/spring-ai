package com.yanzhiyu.springai.mq;

import com.yanzhiyu.springai.entity.dto.ChatTypeDTO;
import com.yanzhiyu.springai.entity.dto.MsgDTO;
import com.yanzhiyu.springai.mapper.ChatTypeMapper;
import com.yanzhiyu.springai.mapper.MsgMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author yanzhiyu
 * @date 2025/7/19
 */
@Service
public class MsgConsumerService {

    @Resource
    private MsgMapper msgMapper;

    @Resource
    private ChatTypeMapper chatTypeMapper;

    @KafkaListener(topics = "msg-topic", groupId = "msg-consumer-group")
    @Transactional
    public void consume(MsgDTO msgDTO) {
        System.out.println("Consumed message: " + msgDTO);
        msgMapper.insert(msgDTO);
        System.out.println("Message saved to database");
    }

    @KafkaListener(topics = "chat-type-topic", groupId = "msg-consumer-group")
    @Transactional
    public void consume(ChatTypeDTO chatTypeDTO) {
        System.out.println("Consumed message: " + chatTypeDTO);
        chatTypeMapper.insert(chatTypeDTO);
        System.out.println("Message saved to database");
    }
}
