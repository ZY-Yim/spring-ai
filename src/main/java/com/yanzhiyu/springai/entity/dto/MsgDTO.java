package com.yanzhiyu.springai.entity.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
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

}
