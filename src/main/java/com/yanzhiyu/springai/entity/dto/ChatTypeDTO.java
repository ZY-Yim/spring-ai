package com.yanzhiyu.springai.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yanzhiyu
 * @date 2025/7/20
 */
@Data
public class ChatTypeDTO  implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private LocalDateTime createTime;

    private String chatType;

    private String chatId;

}
