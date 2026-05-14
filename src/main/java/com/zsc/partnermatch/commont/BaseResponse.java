package com.zsc.partnermatch.commont;

import lombok.Data;

/**
 * 通用返回对象
 * @param <T> 数据类型
 */
@Data
public class BaseResponse<T> {
    private int code;
    private String message;
    private T data;
    private String description;

    public BaseResponse(int code, String message, T data, String description) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.description = description;
    }

    public BaseResponse(int code, String message, T data) {
        this(code, message, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage(), null, errorCode.getDescription());
    }
}