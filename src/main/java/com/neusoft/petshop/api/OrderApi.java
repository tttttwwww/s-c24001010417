package com.neusoft.petshop.api;

import com.neusoft.petshop.common.Result;
import com.neusoft.petshop.dto.CheckoutRequest;
import com.neusoft.petshop.model.Order;
import com.neusoft.petshop.service.IOrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 订单 REST 接口
 *
 *   POST /api/orders   下单结算（校验库存 → 扣库存 → 生成订单）
 *   GET  /api/orders   查询订单列表（演示用，看得到自己刚下的单）
 *
 * ★ 这个接口在第一版里是不存在的。
 *   第一版页面上那个「去结算」按钮只会弹一句「下单成功（演示）」，
 *   库存不减、订单不存，纯粹是演戏。现在它真的会走一遍业务流程。
 */
@RestController
@RequestMapping("/api/orders")
public class OrderApi {

    private final IOrderService orderService;

    public OrderApi(IOrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 下单结算
     *
     * POST /api/orders
     * { "items": [ { "productId": 1, "quantity": 2 }, { "productId": 3, "quantity": 1 } ] }
     *
     * 可能返回的业务错误（都由 GlobalExceptionHandler 统一包装）：
     *   400 购物车是空的，无法下单
     *   400 购买数量必须大于 0（商品 id=1）
     *   404 商品不存在，id=99
     *   400 「皇家猫粮」库存不足，当前仅剩 3 件
     */
    @PostMapping
    public Result<Order> checkout(@RequestBody CheckoutRequest request) {
        Order order = orderService.checkout(request == null ? null : request.getItems());
        return Result.success("下单成功", order);
    }

    /** 订单列表（最新在前） */
    @GetMapping
    public Result<List<Order>> list() {
        return Result.success(orderService.listAll());
    }
}
