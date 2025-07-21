package com.yanzhiyu.springai.repository;

import org.springframework.core.io.Resource;

/**
 * @author yanzhiyu
 * @date 2025/7/6
 */
public interface FileRepository {
    /**
     * 保存文件,还要记录chatId与文件的映射关系
     *
     * @param chatId   会话id
     * @param resource 文件
     * @return 返回唯一一个文件名
     */
    Boolean save(String chatId, Resource resource);

    /**
     * 根据chatId获取文件
     *
     * @param chatId 会话id
     * @return 找到的文件
     */
    Resource getFile(String chatId);

    /**
     * 获取原始文件名
     *
     * @param chatId 会话id
     * @return 文件名
     */
    String getEncodeFileName(String chatId);

    /**
     * 获取唯一文件名
     *
     * @param chatId 会话id
     * @return 文件名
     */
    String getUniqueFileName(String chatId);

    /**
     * 保存文件到向量库
     *
     * @param resource  文件
     * @param uniqueFileName 唯一文件名
     * @param encodeFileName 编码文件名
     */
    void writeToVectorStore(Resource resource, String uniqueFileName, String encodeFileName);
}