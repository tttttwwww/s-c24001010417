package com.neusoft.petshop.model;

import java.math.BigDecimal;

/**
 * 商品实体类：描述一条宠物用品数据
 * 字段：id、名称、分类、价格、库存
 */
public class PetProduct {

    private Integer id;
    private String name;
    private String category;
    private BigDecimal price;
    private Integer stock;

    public PetProduct() {
    }

    public PetProduct(Integer id, String name, String category, BigDecimal price, Integer stock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    @Override
    public String toString() {
        return "PetProduct{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                '}';
    }
}
