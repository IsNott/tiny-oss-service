package com.nott.minio;

import com.nott.minio.propertie.MinioProp;
import io.minio.*;
import io.minio.http.Method;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Nott
 * @Date 2023/8/8
 */

@Component
public class MinioTemplate {

    private MinioClient client;

    @Resource
    private MinioProp minioProp;

    public MinioClient getClient() throws Exception{
        if(this.client == null){
            MinioClient minioClient =
                    MinioClient.builder()
                            .endpoint(minioProp.getUrl(),minioProp.getPort(),false)
                            .credentials(minioProp.getUsername(), minioProp.getPassword())
                            .build();
            if(minioClient == null){
                throw new RuntimeException("链接远程Minio服务端失败");
            }
            this.client = minioClient;
        }
        boolean found = client.bucketExists(BucketExistsArgs.builder().bucket(minioProp.getBucket()).build());
        if (!found) {
            // Make a new bucket called 'asiatrip'.
            throw new RuntimeException(String.format("没有找到Bucket：[%s]",minioProp.getBucket()));
        }
        return this.client;
    }

    public String upload(MultipartFile file,String holderCode) throws Exception{
        if(file == null){
            throw new RuntimeException("文件不能为空");
        }
        String filePath = "";
        String uuidFileName = UUID.randomUUID().toString();
        if(StringUtils.isNotEmpty(holderCode)){
            filePath = holderCode + "/" + uuidFileName;
        }else {
            filePath = uuidFileName;
        }
        MinioClient minioClient = getClient();
        InputStream inputStream = file.getInputStream();
        minioClient.putObject(
                PutObjectArgs.builder().bucket(minioProp.getBucket()).object(filePath)
                        .contentType(file.getContentType())
                        .stream(
                        inputStream, inputStream.available(), -1)
                        .build());
        return uuidFileName;
    }

    public String getPreviewUrl(String fileName) throws Exception{
        MinioClient client = this.getClient();
        String url =
                client.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .method(Method.GET)
                                .bucket(minioProp.getBucket())
                                .object(fileName)
                                .expiry(1, TimeUnit.DAYS)
                                .build());

        return url;
    }

    public void removeObject(String filePath) throws Exception{
        MinioClient client = this.getClient();
        client.removeObject(
                RemoveObjectArgs.builder().bucket(minioProp.getBucket()).object(filePath).build());
    }

    public void download(String filePath, String fileName,HttpServletResponse response) throws Exception{
        MinioClient client = this.getClient();
        // 创建输入流
        InputStream is = null;
        try {
            // 获取对象的元数据
            StatObjectResponse stat = client.statObject(StatObjectArgs.builder().bucket(minioProp.getBucket()).object(filePath).build());
            // 响应 设置内容类型
            response.setContentType(stat.contentType());
            // 响应 设置编码格式
            response.setCharacterEncoding("UTF-8");
            // 响应 设置头文件
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            // 输入流
            is = client.getObject(GetObjectArgs.builder().bucket(minioProp.getBucket()).object(filePath).build());
            // 将字节从输入流复制到输出流
            IOUtils.copy(is, response.getOutputStream());
        } catch (Exception e) {
            throw new RuntimeException("下载文件异常", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
