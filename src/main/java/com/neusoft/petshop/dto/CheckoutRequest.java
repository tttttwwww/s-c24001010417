package com.neusoft.petshop.dto;

import java.util.List;

/**
 * 下单请求体（DTO：Data Transfer Object，专门用来接收前端传来的数据）
 *
 * 前端 POST /api/orders 的 JSON 长这样：
 * {
 *   "items": [
 *     { "productId": 1, "quantity": 2 },
 *     { "productId": 3, "quantity": 1 }
 *   ]
 * }
 *
 * 为什么不直接用 model 里的 Order 来接收？
 * 因为前端下单时不该决定「订单号是什么」「总价是多少」——
 * 那些是后端算出来的。用一个只含前端该传的字段的 DTO，
 * 从结构上就杜绝了前端篡改金额的可能。
 * 这是参数校验里「永远不要相信前端」的第一课。
 */
public class CheckoutRequest {

    private List<Item> items;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    /** 购物车里的一行：买哪个商品、买几件 */
    public static class Item {

        private Integer productId;
        private Integer quantity;

        public Integer getProductId() {
            return productId;
        }

        public void setProductId(Integer productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
