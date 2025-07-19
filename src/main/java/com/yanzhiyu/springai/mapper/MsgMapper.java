package com.yanzhiyu.springai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yanzhiyu.springai.entity.dto.MsgDTO;
import com.yanzhiyu.springai.entity.pojo.Course;
import com.yanzhiyu.springai.entity.pojo.Msg;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

/**
 * @author yanzhiyu
 * @date 2025/7/19
 */
@Mapper
public interface MsgMapper extends BaseMapper<Course> {

    void insert(MsgDTO msgDTO);
}
