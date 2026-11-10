package com.campus.market.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回体：所有 Controller 的返回值都用它包一层，前端只需判断 code。
 */
@Data
public class Result<T> implements Serializable {

    /** 200 成功 / 4xx 业务错 / 500 系统错 */
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "success";
        r.data = data;
        return r;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }
}
