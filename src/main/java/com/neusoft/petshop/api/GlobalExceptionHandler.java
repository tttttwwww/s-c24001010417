package com.neusoft.petshop.api;

import com.neusoft.petshop.common.BusinessException;
import com.neusoft.petshop.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局异常处理器：让任何异常都变成统一的 Result 结构
 *
 * ★ 没有它会怎样？
 *   接口里一抛异常，Spring 默认返回它自己的错误页/错误 JSON：
 *   { "timestamp": "...", "status": 500, "error": "Internal Server Error", "path": "/api/products/abc" }
 *   字段名跟我们的 Result 完全不一样，前端就得为「正常返回」和「出错返回」
 *   写两套解析逻辑。有了这个类，前端永远只处理 code / message / data。
 *
 * ★ @RestControllerAdvice 的作用范围是「所有 @RestController」，
 *   所以每个接口都不用写 try-catch，异常会自己飘到这里来。
 *
 * ★ 为什么要分这么多种异常来分别处理？
 *   因为它们的「责任人」不同，返回给用户的话术也不同：
 *     BusinessException          业务规则不满足 → 用户能看懂、能自己改（400/404），原样返回消息
 *     MethodArgumentTypeMismatch 路径/参数类型不对 → 用户传错了格式，提示哪个参数错了
 *     HttpMessageNotReadable     请求体 JSON 格式不对 → 提示检查 JSON
 *     Exception                  其它一切 → 这是程序 bug，记日志，但只给用户一句「服务器内部错误」
 *                                 （绝不能把堆栈信息返回给前端，那会泄漏代码结构，也不安全）
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常：最常走的一条
     *
     * 注意这里没有写 @ResponseStatus，所以 HTTP 状态码仍然是 200，
     * 错误信息在响应体的 code 字段里。这是国内课程项目里最常见的「统一响应体」约定：
     * 前端只看 body.code 判断成败。
     * （如果要做成严格的 REST 风格，可以给它加上 @ResponseStatus，让 HTTP 状态码也跟着变。）
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数类型不匹配，例如 GET /api/products/abc（id 要的是数字，传了字母）
     *
     * e.getName() 就是出问题的参数名，把它带上，用户一看就知道该改哪个参数。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型错误：{} = {}", e.getName(), e.getValue());
        return Result.error(400, "参数格式不正确：" + e.getName());
    }

    /**
     * 请求体读不出来：POST/PUT 时 JSON 少个括号、字段类型写错（price 传了 "abc"）都会走这里
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败：{}", e.getMessage());
        return Result.error(400, "请求体格式不正确，请检查提交的 JSON 数据");
    }

    /**
     * 兜底：其它所有没被上面接住的异常
     *
     * 这里必须打完整堆栈日志（log.error 带异常对象），否则线上出了 bug 你什么都查不到；
     * 但返回给用户的只有一句笼统的话 —— 日志给程序员看，提示给用户看，两者要分开。
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "服务器内部错误，请稍后再试");
    }
}
