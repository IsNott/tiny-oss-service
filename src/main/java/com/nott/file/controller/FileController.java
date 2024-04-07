package com.nott.file.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nott.common.CommonPageService;
import com.nott.common.R;
import com.nott.file.entity.SysMinioFile;
import com.nott.file.service.FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Nott
 * @Date 2023/8/8
 */

@RestController
@RequestMapping("file")
public class FileController {

    @Resource
    private FileService fileService;
    @Resource
    private CommonPageService<SysMinioFile> commonPageService;

    @PostMapping("upload")
    public R fileUpload(@RequestParam("file") MultipartFile file, String holderCode) throws Exception {
        if (file == null) {
            return R.failure("文件不能为空");
        }
        return R.okData(fileService.uploadFile(file, holderCode));
    }

    @RequestMapping("preview/{id}")
    public R preview(@NotNull @PathVariable("id") String id) throws Exception {
        String url = fileService.preview(id);
        return R.okData(url);
    }

    @PostMapping("preview")
    public R preview(@NotNull @RequestBody List<String> ids) throws Exception {
        List<String> urls = fileService.preview(ids);
        return R.okData(urls);
    }

    @PostMapping("delById/{id}")
    public R delById(@NotNull @PathVariable("id") String id) throws Exception {
        fileService.delFileById(id);
        return R.ok();
    }

    @RequestMapping("download/{id}")
    public R download(@NotNull @PathVariable("id") String id, HttpServletResponse response) throws Exception {
        fileService.downloadFileById(id,response);
        return R.ok();
    }

    @PostMapping("page")
    public R page(@RequestBody JSONObject req) {
        QueryWrapper<SysMinioFile> wrapper = null;
        try {
            wrapper = commonPageService.initMbpWrapper(req);
        } catch (Exception e) {
            return R.failure(e.getMessage());
        }
        return R.okData(fileService.page(new Page<>(),wrapper));

    }

//    @PostMapping("batchUpload")
//    public R batchUpload(@RequestBody JSONObject req){
//        try {
//            fileService.batchUploadMovieImagesDir(req.getString("path"));
//        } catch (Exception e) {
//            return R.failure(e.getMessage());
//        }
//        return R.ok();
//    }
}
