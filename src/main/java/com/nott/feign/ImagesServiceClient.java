package com.nott.feign;


import com.alibaba.fastjson.JSONObject;
import com.nott.common.R;
import com.nott.movie.entity.SysMovieInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.constraints.NotNull;


@FeignClient(value = "tiny-images-service")
public interface ImagesServiceClient {

    @PostMapping("movie/saveOrUpdate")
    R saveOrUpdate(SysMovieInfo sysMovieInfo);

    @RequestMapping("movie/getById/{id}")
    R getById(@NotNull @PathVariable String id);

    @RequestMapping("movie/page")
    R page(@RequestBody JSONObject req);
}
