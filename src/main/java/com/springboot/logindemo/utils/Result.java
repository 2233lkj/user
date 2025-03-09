package com.springboot.logindemo.utils;

public class Result<T> {
    private String code;
    private T data;
    private String msg;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Result() {

    }

    public Result(T data) {
        this.data = data;
    }


    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>(data);
        result.setCode("0");
        result.setMsg("成功");
        return result;
    }

    /*public static <T> Result<T> success(T data, String msg) {
        Result<T> result = new Result<>(data);
        result.setCode("0");
        result.setMsg(msg);
        return result;
    }*/

    public static <T> Result<T> success(T object, String msg) {
        Result<T> r = new Result<T>();
        r.data = object;
        r.code = "200";
        r.msg = msg;
        return r;
    }

    public static Result error(String code, String msg) {
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}
