package com.nott.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author Nott
 * @Date 2023/8/8
 */
@Data
@TableName("sys_minio_file")
public class SysMinioFile {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String holderCode;

    private Date createTime;

    private String contentType;

    private Date updateTime;

    private String bucketName;

    private String originName;

    private String fileName;
}
