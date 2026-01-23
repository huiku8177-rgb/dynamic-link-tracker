package com.neozeng.trackerserve.exception;

import com.neozeng.trackerserve.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 未登录 / 未授权
     */
    @ExceptionHandler(UnAuthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleUnAuthorizedException(UnAuthorizedException e) {
        log.warn("未授权访问: {}", e.getMessage());
        Result<Void> result = new Result<>();
        result.setCode(401);
        result.setMessage(e.getMessage());
        result.setData(null);
        return result;
    }

    /**
     * 兜底异常处理，避免错误信息泄露
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("服务器内部错误", e);
        return Result.error("服务器繁忙，请稍后重试");
    }
}


