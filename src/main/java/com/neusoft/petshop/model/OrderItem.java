package com.neusoft.petshop.model;

import java.math.BigDecimal;

/**
 * 订单明细实体：订单里的一行（买了哪个商品、买几个、单价多少、小计多少）
 *
 * 为什么要有这个类，而不是订单里直接塞 PetProduct？
 * 因为商品的价格会变、名称会改、下架会被删。订单是「历史凭证」，
 * 必须把下单那一刻的名称和单价抄一份存下来，否则以后改个价，历史订单金额就跟着变了。
 * 这是电商系统里非常典型的一个设计点。
 */
public class OrderItem {

    private Integer productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;

    public OrderItem() {
    }

    public OrderItem(Integer productId, String productName, BigDecimal price, Integer quantity) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public String toString() {
        return productName + " ¥" + price + " × " + quantity + " = ¥" + subtotal;
    }
}
