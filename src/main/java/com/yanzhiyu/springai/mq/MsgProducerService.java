package com.yanzhiyu.springai.mq;

import com.yanzhiyu.springai.entity.dto.MsgDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * @author yanzhiyu
 * @date 2025/7/19
 */
@Service
public class MsgProducerService {
    private static final String TOPIC = "msg-topic";

    private final KafkaTemplate<String, MsgDTO> kafkaTemplate;

    public MsgProducerService(KafkaTemplate<String, MsgDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(MsgDTO msgDTO) {
        kafkaTemplate.send(TOPIC, msgDTO);
        System.out.println("Produced message: " + msgDTO);
    }
}
