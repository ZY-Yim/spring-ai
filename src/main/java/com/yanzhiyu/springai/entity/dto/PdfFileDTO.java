package com.yanzhiyu.springai.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yanzhiyu
 * @date 2025/7/20
 */
@Data
public class PdfFileDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String chatId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String uniqueFileName;

    private String encodeFileName;
}
