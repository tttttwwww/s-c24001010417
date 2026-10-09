package com.neusoft.petshop.service;

import com.neusoft.petshop.dto.CheckoutRequest;
import com.neusoft.petshop.model.Order;

import java.util.List;

/**
 * 订单服务接口
 *
 * 订单是「商品」之外的另一个业务领域，所以单独一个 Service，
 * 而不是把 checkout 塞进 IPetProductService 里。
 * 一个 Service 类管一个领域，是分层架构里划分边界的常规做法。
 */
public interface IOrderService {

    /**
     * 结算下单
     *
     * 要做的事：校验购物车 → 校验库存 → 扣库存 → 生成订单。
     * 任何一步不满足业务规则，抛 BusinessException，消息可以直接显示给用户。
     *
     * @param items 购物车明细（商品 id + 数量）
     * @return 生成的订单（含订单号、明细快照、总金额、下单时间）
     */
    Order checkout(List<CheckoutRequest.Item> items);

    /** 查询全部订单（最新在前） */
    List<Order> listAll();

    /** 订单总数 */
    int count();
}
