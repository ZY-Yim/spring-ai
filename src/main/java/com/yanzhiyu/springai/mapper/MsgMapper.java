package com.yanzhiyu.springai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yanzhiyu.springai.entity.dto.MsgDTO;
import com.yanzhiyu.springai.entity.pojo.Course;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author yanzhiyu
 * @date 2025/7/19
 */
@Mapper
public interface MsgMapper {

    void insert(MsgDTO msgDTO);

    List<MsgDTO> getMsgList(String chatId);

}
