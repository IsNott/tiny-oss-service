package com.nott.minio;

import com.nott.file.controller.FileController;
import com.nott.minio.propertie.MinioProp;
import io.minio.*;
import io.minio.http.Method;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
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

    private final Logger log = LoggerFactory.getLogger(MinioTemplate.class);

    private MinioClient client;
    @Resource
    private MinioProp minioProp;
    @Value("${upload.maxSize}")
    private long maxSize;
    @Value("${upload.compress}")
    private boolean compress;

    @Value("${minio.autocreate}")
    public boolean autoCreate;

    public MinioClient getClient() throws Exception {
        if (this.client == null) {
            MinioClient minioClient =
                    MinioClient.builder()
                            .endpoint(minioProp.getUrl(), minioProp.getPort(), false)
                            .credentials(minioProp.getUsername(), minioProp.getPassword())
                            .build();
            if (minioClient == null) {
                throw new RuntimeException("链接远程Minio服务端失败");
            }
            this.client = minioClient;
        }
        boolean found = client.bucketExists(BucketExistsArgs.builder().bucket(minioProp.getBucket()).build());
        if (!found) {
            if (autoCreate) {
                client.makeBucket(MakeBucketArgs.builder().bucket(minioProp.getBucket()).build());
                log.info("Auto make bucket: {}", minioProp.getBucket());
            } else {
                throw new RuntimeException(String.format("没有找到Bucket：[%s]", minioProp.getBucket()));
            }
        }

        return this.client;
    }

    public String upload(MultipartFile file, String holderCode) throws Exception {
        if (file == null) {
            throw new RuntimeException("文件不能为空");
        }

        boolean isPic = this.isPicture(file);
        String filePath = "";
        String uuidFileName = UUID.randomUUID().toString();
        if (StringUtils.isNotEmpty(holderCode)) {
            filePath = holderCode + "/" + uuidFileName;
        } else {
            filePath = uuidFileName;
        }
        MinioClient minioClient = getClient();
        // 如果是图片则压缩
        if (isPic && compress) {
            file = this.handlePicCompress(file);
        }
        InputStream inputStream = file.getInputStream();
        minioClient.putObject(
                PutObjectArgs.builder().bucket(minioProp.getBucket()).object(filePath)
                        .contentType(file.getContentType())
                        .stream(
                                inputStream, inputStream.available(), -1)
                        .build());
        log.info("File {},Upload Bucket {}", uuidFileName, minioProp.getBucket());
        return uuidFileName;
    }

    private MultipartFile handlePicCompress(MultipartFile file) throws Exception {
        if (file.getSize() > maxSize) {
            String surffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String fileName = StringUtils.isNotEmpty(file.getName()) ? file.getName() : UUID.randomUUID().toString().replaceAll("-", "");
            String path = System.getProperty("java.io.tmpdir") + File.separator;
            // 在项目根目录下的 upload 目录中生成临时文件
            File newFile = new File(path + UUID.randomUUID().toString() + surffix);
            // 小于 1M 的
            if ((1024 * 1024 * 0.1) <= file.getSize() && file.getSize() <= (1024 * 1024)) {
                Thumbnails.of(file.getInputStream()).scale(1f).outputQuality(0.3f).toFile(newFile);
            }
            // 1 - 2M 的
            else if ((1024 * 1024) < file.getSize() && file.getSize() <= (1024 * 1024 * 2)) {
                Thumbnails.of(file.getInputStream()).scale(1f).outputQuality(0.2f).toFile(newFile);
            }
            // 2M 以上的
            else if ((1024 * 1024 * 2) < file.getSize()) {
                Thumbnails.of(file.getInputStream()).scale(1f).outputQuality(0.1f).toFile(newFile);
            }
            // 获取输入流
            FileInputStream input = new FileInputStream(newFile);
            // 转为 MultipartFile
            return new MockMultipartFile(fileName, file.getOriginalFilename(), file.getContentType(), input);
        } else {
            return file;
        }
    }

    private boolean isPicture(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType.contains("image/")) {
            return true;
        }
        return false;
    }

    public String getPreviewUrl(String fileName) throws Exception {
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

    public void removeObject(String filePath) throws Exception {
        MinioClient client = this.getClient();
        client.removeObject(
                RemoveObjectArgs.builder().bucket(minioProp.getBucket()).object(filePath).build());
    }

    public void download(String filePath, String fileName, HttpServletResponse response) throws Exception {
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
