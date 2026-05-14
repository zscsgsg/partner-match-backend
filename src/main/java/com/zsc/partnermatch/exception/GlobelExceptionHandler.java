package com.zsc.partnermatch.exception;

import com.zsc.partnermatch.commont.BaseResponse;
import com.zsc.partnermatch.commont.ErrorCode;
import com.zsc.partnermatch.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@Hidden
public class GlobelExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<String> businessExceptionHandler(BusinessException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return ResultUtils.error(e.getCode(), e.getMessage(), e.getDescription());
    }

    @ExceptionHandler(Exception.class)
    public BaseResponse<String> runtimeExceptionHandler(Exception e) {
        log.error("系统异常: ", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage());
    }
}
