package com.yanzhiyu.springai.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.OSSObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author yanzhiyu
 * @date 2025/7/17
 */
@Data
@AllArgsConstructor
@Slf4j
public class OssUtil {
    private String endpoint;
    private String bucketName;
    private String fileHost;
    private final OSS ossClient;

    /**
     * 文件上传
     *
     * @param bytes 文件字节数组
     * @param objectName 文件名
     * @return 文件访问路径
     */
    public String upload(byte[] bytes, String objectName) {

        try {
            // 创建PutObject请求。
            ossClient.putObject(bucketName, fileHost + "/" + objectName, new ByteArrayInputStream(bytes));
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        }

        //文件访问路径规则 https://BucketName.Endpoint/ObjectName
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder
                .append(bucketName)
                .append(".")
                .append(endpoint)
                .append("/").append(fileHost).append("/").append(objectName);

        log.info("文件上传到:{}", stringBuilder.toString());

        return stringBuilder.toString();
    }

    /**
     * 下载文件并返回字节数组
     *
     * @param objectName 文件名（相对路径）
     * @return 文件字节数据
     */
    public byte[] download(String objectName) {
        String fullObjectName = fileHost + "/" + objectName;

        try {
            OSSObject ossObject = ossClient.getObject(bucketName, fullObjectName);
            InputStream inputStream = ossObject.getObjectContent();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("文件下载失败: {}", objectName, e);
            throw new RuntimeException("文件下载失败", e);
        }
    }
}
