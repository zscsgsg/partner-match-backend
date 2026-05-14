package com.zsc.partnermatch.utils;

import com.zsc.partnermatch.commont.BaseResponse;
import com.zsc.partnermatch.commont.ErrorCode;

/**
 * 返回结果工具类
 */
public class ResultUtils {

    /**
     * 成功
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, "ok", data);
    }

    /**
     * 失败（使用错误码枚举）
     */
    public static BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败（自定义错误码和描述）
     */
    public static BaseResponse error(int code, String message, String description) {
        return new BaseResponse<>(code, message, null, description);
    }

    /**
     * 失败（仅错误码枚举，额外附加描述）
     */
    public static BaseResponse error(ErrorCode errorCode, String description) {
        return new BaseResponse<>(errorCode.getCode(), errorCode.getMessage(), null, description);
    }
}
