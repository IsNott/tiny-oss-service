package com.nott.common;

import java.io.Serializable;

/**
 * @author Nott
 * @Date 2023/8/8
 */


public class R implements Serializable {
    private String code;
    private String msg;
    private Object obj;

    public R(String code, String msg, Object obj) {
        this.code = code;
        this.msg = msg;
        this.obj = obj;
    }

    public R() {
    }


    public static R okData(String msg, Object obj) {
        R result = new R();
        result.setCode("200");
        result.setMsg(msg);
        result.setObj(obj);
        return result;
    }

    public static R ok(String msg) {
        R result = new R();
        result.setCode("200");
        result.setMsg(msg);
        return result;
    }

    public static R ok() {
        return ok("success");
    }

    public static R okData(Object obj) {
        R result = new R();
        result.setCode("200");
        result.setMsg("success");
        result.setObj(obj);
        return result;
    }

    public static R failure(String msg) {
        R result = new R();
        result.setCode("-999");
        result.setMsg(msg);
        return result;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}

