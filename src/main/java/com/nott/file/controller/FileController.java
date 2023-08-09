package com.nott.file.controller;

import com.nott.common.R;
import com.nott.file.service.FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

/**
 * @author Nott
 * @Date 2023/8/8
 */

@RestController
@RequestMapping("file")
public class FileController {

    @Resource
    private FileService fileService;

    @PostMapping("upload")
    public R fileUpload(@RequestParam("file") MultipartFile file, String holderCode) throws Exception {
        if (file == null) {
            return R.failure("文件不能为空");
        }
        return R.okData(fileService.uploadFile(file, holderCode));
    }

    @PostMapping("preview/{id}")
    public R fileUpload(@NotNull @PathVariable("id") String id) throws Exception {
        String url = fileService.preview(id);
        return R.okData(url);
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
}
