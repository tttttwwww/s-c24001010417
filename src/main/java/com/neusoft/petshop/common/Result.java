package com.neusoft.petshop.common;

/**
 * 统一响应结果
 *
 * 前端永远只解析这 3 个字段，接口返回结构固定，前端就不用为每个接口写一套解析逻辑：
 * {
 *   "code": 200,
 *   "message": "操作成功",
 *   "data": ...
 * }
 *
 * code 约定：
 *   200  成功
 *   400  参数不合法（比如价格填了负数）
 *   404  数据不存在（比如查一个不存在的商品 id）
 *   500  服务器内部错误
 *
 * @param <T> data 的具体类型
 */
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    public Result() {
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ---------------- 成功 ----------------

    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    // ---------------- 失败 ----------------

    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    // ---------------- getter / setter ----------------
    // 没引 Lombok，手写 setter 是因为 Jackson 把对象转 JSON 时要靠 getter;
    // 把 JSON 转成对象时要靠无参构造器 + setter。

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
