package com.zsc.partnermatch.commont;

import lombok.Getter;

/**
 * 错误码枚举
 * 这个 ErrorCode 枚举正是为了配合全局异常处理器 (GlobalExceptionHandler) 使用的。
 *
 * 这个抛出 throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        PARAMS_ERROR(40000, "请求参数错误", ""),这个里面有参数
 *  public BaseResponse handleBusinessException(BusinessException e) {
 *     return new BaseResponse(e.getCode(), e.getMessage(), null, e.getDescription());
 * }这个全局接收， 接收里面的参数，还有描述，同时返回前端 的错误信息
 *
 */
@Getter
public enum ErrorCode {
    SUCCESS(0, "ok", ""),
    PARAMS_ERROR(40000, "请求参数错误", ""),
    NULL_ERROR(40001, "请求数据为空", ""),
    NOT_LOGIN(40100, "未登录", ""),
    NO_AUTH(40101, "无权限", ""),
    FORBIDDEN(40301, "禁止操作", ""),
    SYSTEM_ERROR(50000, "系统内部异常", "");


    private final int code;
    private final String message;
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }




}
