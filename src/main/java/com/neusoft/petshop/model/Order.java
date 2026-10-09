package com.neusoft.petshop.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体：一次结算产生的订单
 *
 * orderNo 用「PS + 时间 + 序号」拼出来（PS = Pet Shop）。
 * 真实项目里订单号一般由专门的发号器生成，这里用时间戳已经够演示，
 * 好处是肉眼能看出下单时间，排查问题方便。
 */
public class Order {

    /** 订单号，如 PS20261009103015001 */
    private String orderNo;

    /** 订单明细 */
    private List<OrderItem> items;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 下单时间（@JsonFormat 控制输出成 yyyy-MM-dd HH:mm:ss，而不是一串 ISO 格式） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    public Order() {
    }

    public Order(String orderNo, List<OrderItem> items, BigDecimal totalAmount, LocalDateTime createTime) {
        this.orderNo = orderNo;
        this.items = items;
        this.totalAmount = totalAmount;
        this.createTime = createTime;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "订单 " + orderNo + "，共 " + items.size() + " 种商品，合计 ¥" + totalAmount;
    }
}
