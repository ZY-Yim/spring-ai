package com.yanzhiyu.springai.mapper;

import com.yanzhiyu.springai.entity.dto.MsgDTO;
import com.yanzhiyu.springai.entity.dto.PdfFileDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author yanzhiyu
 * @date 2025/7/19
 */
@Mapper
public interface PdfFileMapper {

    void insert(PdfFileDTO pdfFileDTO);

    PdfFileDTO getPdfFile(String chatId);

    void update(PdfFileDTO pdfFileDTO);
}
