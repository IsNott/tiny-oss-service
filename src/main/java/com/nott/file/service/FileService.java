package com.nott.file.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.util.UuidUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nott.common.R;
import com.nott.common.ThreadPoolContext;
import com.nott.feign.ImagesServiceClient;
import com.nott.file.entity.SysMinioFile;
import com.nott.file.mapper.SysMinioFileMapper;
import com.nott.minio.MinioTemplate;
import com.nott.minio.propertie.MinioProp;
import com.nott.movie.entity.SysMovieInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * @author Nott
 * @Date 2023/8/8
 */

@Service
@Slf4j
public class FileService extends ServiceImpl<SysMinioFileMapper, SysMinioFile> {

    @Resource
    private MinioTemplate minioTemplate;
    @Resource
    private MinioProp minioProp;

    @Transactional(rollbackFor = Exception.class)
    public SysMinioFile uploadFile(MultipartFile file, String holderCode) throws Exception {
        String fileUuIdFileName = minioTemplate.upload(file, holderCode);

        SysMinioFile sysMinioFile = new SysMinioFile();
        sysMinioFile.setOriginName(file.getOriginalFilename());
        sysMinioFile.setFileName(fileUuIdFileName);
        sysMinioFile.setHolderCode(holderCode);
        sysMinioFile.setBucketName(minioProp.getBucket());
        sysMinioFile.setCreateTime(new Date());
        sysMinioFile.setContentType(file.getContentType());
        this.save(sysMinioFile);
        return sysMinioFile;
    }

    public String preview(String id) throws Exception {
        SysMinioFile sysMinioFile = this.getById(id);
        if (sysMinioFile == null || StringUtils.isEmpty(sysMinioFile.getFileName())) {
            throw new RuntimeException("传入的文件id有误");
        }
        String holderCode = sysMinioFile.getHolderCode();
        String fileName = sysMinioFile.getFileName();
        String query = StringUtils.isNotEmpty(holderCode) ? holderCode + "/" + fileName : fileName;
        return minioTemplate.getPreviewUrl(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delFileById(String id) throws Exception {
        SysMinioFile sysMinioFile = this.getById(id);
        if (sysMinioFile == null || StringUtils.isEmpty(sysMinioFile.getFileName())) {
            throw new RuntimeException("传入的文件id有误");
        }
        String holderCode = sysMinioFile.getHolderCode();
        String fileName = sysMinioFile.getFileName();
        String query = StringUtils.isNotEmpty(holderCode) ? holderCode + "/" + fileName : fileName;
        minioTemplate.removeObject(query);
    }

    public void downloadFileById(String id, HttpServletResponse response) throws Exception {
        SysMinioFile sysMinioFile = this.getById(id);
        if (sysMinioFile == null || StringUtils.isEmpty(sysMinioFile.getFileName())) {
            throw new RuntimeException("传入的文件id有误");
        }
        String holderCode = sysMinioFile.getHolderCode();
        String fileName = sysMinioFile.getFileName();
        String query = StringUtils.isNotEmpty(holderCode) ? holderCode + "/" + fileName : fileName;
        minioTemplate.download(query, sysMinioFile.getOriginName(), response);
    }

//    @Transactional(rollbackFor = Exception.class)
//    public void batchUploadMovieImagesDir(String localPath) throws Exception {
//        File file = new File(localPath);
//        HashMap<String,String> map = new HashMap<>(16);
//        if (!file.exists()) {
//            throw new RuntimeException("传入的文件夹地址有误");
//        }
//        log.info("当前路径：{}",localPath);
//        if (file.isDirectory()) {
//            File[] files = file.listFiles();
//            if (files == null || files.length == 0) {
//                log.error(String.format("文件地址[%s]没有数据", localPath));
//                return;
//            }
//            List<File> fileList = Arrays.asList(files);
//            Iterator<File> iterator = fileList.iterator();
//            Long count = 0L;
//            String movieUUid = "";
//            while (iterator.hasNext()) {
//                File child = iterator.next();
//                File parent = child.getParentFile();
//                String holderName = parent.getName();
//                if (child.isDirectory()) {
//                    this.batchUploadMovieImagesDir(child.getPath());
//                } else if (child.isFile()) {
//                    // 保存电影信息
//                    movieUUid = this.saveMovieInfo(holderName);
//                    map.put(movieUUid,holderName);
//                    // 上传
//                    FileInputStream inputStream = new FileInputStream(child);
//                    MultipartFile multipartFile = new MockMultipartFile(child.getName(), "", MediaTypeFactory.getMediaType(child.getName()).orElse(MediaType.APPLICATION_OCTET_STREAM).toString(), inputStream);
//                    this.uploadFile(multipartFile, movieUUid);
//                    count ++;
//                }
//            }
//            if(StringUtils.isNotEmpty(map.get(movieUUid))){
//                log.info("文件路径{}，上传完成{}个文件",localPath + File.separator + map.get(movieUUid),count);
//            }
//        }
//
//
//    }
//
//    private String saveMovieInfo(String fileName) {
//        String years = null;
//        if (fileName.contains("（") && fileName.contains("）")) {
//            years = fileName.substring(fileName.lastIndexOf("（") + 1, fileName.lastIndexOf("）"));
//            fileName = fileName.substring(0, fileName.lastIndexOf("（"));
//        }
//        if (fileName.contains("(") && fileName.contains(")")) {
//            years = fileName.substring(fileName.lastIndexOf("(") + 1, fileName.lastIndexOf(")"));
//            fileName = fileName.substring(0, fileName.lastIndexOf("("));
//        }
//        String id = null;
//        // todo 创建SQL查询类
//        JSONObject req = new JSONObject();
//        JSONArray objects = new JSONArray();
//        JSONObject query = new JSONObject();
//        query.put("attr", "pub_name");
//        query.put("val", fileName);
//        query.put("expression", "eq");
//        objects.add(query);
//        req.put("query", objects);
//
//        R r = imagesServiceClient.page(req);
//        Page<SysMovieInfo> page = (Page<SysMovieInfo>) r.getObj();
//        if (page.getTotal() == 0) {
//            id = UUID.randomUUID().toString().replaceAll("-", "");
//            SysMovieInfo sysMovieInfo = new SysMovieInfo();
//            sysMovieInfo.setId(id);
//            sysMovieInfo.setPubName(fileName);
//            sysMovieInfo.setYear(years);
//            imagesServiceClient.saveOrUpdate(sysMovieInfo);
//        } else {
//            id = page.getRecords().get(0).getId();
//        };
//        return id;
//    }
}
