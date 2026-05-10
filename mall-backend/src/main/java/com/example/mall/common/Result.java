package com.example.mall.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code; //状态码
    private String msg;   //提示信息
    private T data;       //返回的数据

    //成功时的静态方法
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMsg("操作成功");
        r.setData(data);
        return r;
    }

    //失败时的静态方法
    public static <T> Result<T> error(String msg) {
        Result<T> r = new Result<>();
        r.setCode(500);
        r.setMsg(msg);
        return r;
    }
}