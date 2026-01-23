package com.neozeng.trackerserve.exception;

/**
 * 未授权/未登录异常
 * 抛出该异常后由全局异常处理器转换为 401 响应
 */
public class UnAuthorizedException extends RuntimeException {

    public UnAuthorizedException() {
        super("未登录或登录已过期，请先登录");
    }

    public UnAuthorizedException(String message) {
        super(message);
    }
}


