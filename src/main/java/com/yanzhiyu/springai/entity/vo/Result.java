package com.yanzhiyu.springai.entity.vo;

import lombok.Data;

/**
 * @author yanzhiyu
 * @date 2025/7/6
 */
@Data
public class Result {
    private Integer ok;
    private String msg;

    private Result(Integer ok, String msg) {
        this.ok = ok;
        this.msg = msg;
    }

    public static Result ok() {
        return new Result(1, "ok");
    }

    public static Result fail(String msg) {
        return new Result(0, msg);
    }
}
