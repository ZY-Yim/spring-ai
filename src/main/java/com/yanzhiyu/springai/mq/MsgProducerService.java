package com.yanzhiyu.springai.mq;

import com.yanzhiyu.springai.entity.dto.ChatTypeDTO;
import com.yanzhiyu.springai.entity.dto.MsgDTO;
import com.yanzhiyu.springai.entity.dto.PdfFileDTO;
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

    private static final String PDF_FILE_TOPIC = "pdf-file-topic";

    private final KafkaTemplate<String, MsgDTO> msgKafkaTemplate;

    private final KafkaTemplate<String, ChatTypeDTO> chatTypeKafkaTemplate;

    private final KafkaTemplate<String, PdfFileDTO> pdfFileDTOKafkaTemplate;

    public void sendMessage(MsgDTO msgDTO) {
        msgKafkaTemplate.send(MSG_TOPIC, msgDTO);
        System.out.println("Produced message: " + msgDTO);
    }

    public void sendChatType(ChatTypeDTO chatTypeDTO) {
        chatTypeKafkaTemplate.send(CHAT_TYPE_TOPIC, chatTypeDTO);
        System.out.println("Produced message: " + chatTypeDTO);
    }

    public void sendPdfFile(PdfFileDTO pdfFileDTO) {
        pdfFileDTOKafkaTemplate.send(PDF_FILE_TOPIC, pdfFileDTO);
        System.out.println("Produced message: " + pdfFileDTO);
    }
}
