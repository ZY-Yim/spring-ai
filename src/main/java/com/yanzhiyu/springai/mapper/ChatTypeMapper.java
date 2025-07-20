package com.yanzhiyu.springai.mapper;

import com.yanzhiyu.springai.entity.dto.ChatTypeDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author yanzhiyu
 * @date 2025/7/19
 */
@Mapper
public interface ChatTypeMapper {

    void insert(ChatTypeDTO chatTypeDTO);

    List<ChatTypeDTO> getChatIds(String type);
}
