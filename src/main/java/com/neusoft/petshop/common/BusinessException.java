package com.neusoft.petshop.common;

/**
 * 业务异常：用来表达「规则不满足」而不是「程序出错」
 *
 * 区分两类问题的意义：
 *   程序出错（NullPointerException、数据库连不上）→ 是 bug，要修代码，返回 500
 *   业务规则不满足（价格填了负数、库存不够、id 重复）→ 是正常业务流程，返回 400/404 + 明确提示
 *
 * 以前 Service 用「返回 0」表示失败，问题是：控制台还能猜猜，但浏览器
 * 只能看到一个 0，根本不知道是「id 重复」还是「价格非法」。
 * 抛异常带上 message，再由 GlobalExceptionHandler 统一转成 Result，前端就能直接显示。
 *
 * 继承 RuntimeException（非受检异常）：
 * 这样 Service 方法签名不用写 throws，控制台和 Web 两边的调用代码都不必强制 try-catch。
 */
public class BusinessException extends RuntimeException {

    /** 给前端看的业务错误码：400 参数问题 / 404 数据不存在 */
    private final Integer code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
