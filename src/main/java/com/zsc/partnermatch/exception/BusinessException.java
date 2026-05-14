package com.zsc.partnermatch.exception;

import com.zsc.partnermatch.commont.ErrorCode;
import lombok.Getter;

/**
 * GlobalExceptionHandler 会捕获 BusinessException
 * 然后从中取出 code 和 description，组装成统一的 BaseResponse 返回给前端。
 *public BaseResponse handleBusinessException(BusinessException e) {
 *     // 这里就用到了 e.getCode() 和 e.getDescription()
 *     return new BaseResponse(e.getCode(), e.getMessage(), null, e.getDescription());
 * }
 *
 */
@Getter
public class BusinessException extends RuntimeException {
    private final int code;
    private final String description;

    public BusinessException(String message) {
        super(message);
        this.code = ErrorCode.SYSTEM_ERROR.getCode();
        this.description = "";
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BusinessException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }

    public BusinessException(int code, String message, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

}
