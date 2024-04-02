package com.nott.common;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Nott
 * @date 2024-4-2
 */
@RestControllerAdvice(annotations = RestController.class)
public class ExceptionHandler {

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public R handlerException(Exception e){
        return R.failure(e.getMessage());
    }

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(RuntimeException.class)
    public R handlerRuntimeException(RuntimeException e){
        return R.failure(e.getMessage());
    }

}
