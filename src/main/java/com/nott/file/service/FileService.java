package com.nott.file.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nott.file.entity.SysMinioFile;
import com.nott.file.mapper.SysMinioFileMapper;
import com.nott.minio.MinioTemplate;
import com.nott.minio.propertie.MinioProp;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author Nott
 * @Date 2023/8/8
 */

@Service
public class FileService extends ServiceImpl<SysMinioFileMapper, SysMinioFile> {

    @Resource
    private MinioTemplate minioTemplate;
    @Resource
    private MinioProp minioProp;

    @Transactional(rollbackFor = Exception.class)
    public SysMinioFile uploadFile(MultipartFile file,String holderCode) throws Exception{
        String fileUuIdFileName = minioTemplate.upload(file,holderCode);

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

    public String preview(String id) throws Exception{
        SysMinioFile sysMinioFile = this.getById(id);
        if(sysMinioFile == null || StringUtils.isEmpty(sysMinioFile.getFileName())){
            throw new RuntimeException("传入的文件id有误");
        }
        String holderCode = sysMinioFile.getHolderCode();
        String fileName = sysMinioFile.getFileName();
        String query = StringUtils.isNotEmpty(holderCode) ? holderCode + "/" + fileName : fileName;
        return minioTemplate.getPreviewUrl(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delFileById(String id) throws Exception{
        SysMinioFile sysMinioFile = this.getById(id);
        if(sysMinioFile == null || StringUtils.isEmpty(sysMinioFile.getFileName())){
            throw new RuntimeException("传入的文件id有误");
        }
        String holderCode = sysMinioFile.getHolderCode();
        String fileName = sysMinioFile.getFileName();
        String query = StringUtils.isNotEmpty(holderCode) ? holderCode + "/" + fileName : fileName;
        minioTemplate.removeObject(query);
    }

    public void downloadFileById(String id, HttpServletResponse response) throws Exception{
        SysMinioFile sysMinioFile = this.getById(id);
        if(sysMinioFile == null || StringUtils.isEmpty(sysMinioFile.getFileName())){
            throw new RuntimeException("传入的文件id有误");
        }
        String holderCode = sysMinioFile.getHolderCode();
        String fileName = sysMinioFile.getFileName();
        String query = StringUtils.isNotEmpty(holderCode) ? holderCode + "/" + fileName : fileName;
        minioTemplate.download(query,sysMinioFile.getOriginName(),response);
    }


}
