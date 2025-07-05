package com.yanzhiyu.springai.repository;

import java.util.List;

/**
 * @author yanzhiyu
 * @date 2025/7/5
 */
public interface ChatHistoryRepository {

    /**
     * 保存会话记录
     * @param type 业务类型
     * @param chatId 会话ID
     */
    void save(String type, String chatId);

    /**
     * 获取会话ID列表
     * @param type 业务类型
     * @return 会话ID列表
     */
    List<String> getChatIds(String type);
}
