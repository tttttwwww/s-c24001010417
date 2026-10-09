package com.neusoft.petshop.service.impl;

import com.neusoft.petshop.common.BusinessException;
import com.neusoft.petshop.dao.OrderDao;
import com.neusoft.petshop.dto.CheckoutRequest;
import com.neusoft.petshop.model.Order;
import com.neusoft.petshop.model.OrderItem;
import com.neusoft.petshop.model.PetProduct;
import com.neusoft.petshop.service.IOrderService;
import com.neusoft.petshop.service.IPetProductService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单服务实现：下单这条链路上的业务规则都在这儿
 *
 * ★ 这里最值得看的是「两阶段提交」的写法：
 *
 *   第一阶段：把所有明细挨个检查一遍（商品存在吗？库存够吗？），
 *             检查过程中只收集数据，不动任何库存。
 *   第二阶段：全部检查通过了，才真正去扣库存。
 *
 *   如果边检查边扣，就会出现这种事故：
 *     购物车里 3 件商品，第 1 件扣成功、第 2 件发现库存不足抛异常，
 *     结果第 1 件的库存白白少了，用户却没下成单。
 *   先把所有「不行」的理由找完，再动手，是处理多条数据时的通用思路。
 *
 * ★ 已知简化（真实项目里靠数据库事务解决）：
 *   第二阶段扣库存时如果中途失败，前面已经扣掉的不会自动回滚。
 *   真实系统会把「校验 + 扣减 + 写订单」放在一个数据库事务里，
 *   或者用「库存预占」的方案。本项目的库存是内存 List，没有事务可用，
 *   所以用 synchronized 保证同一个应用内不会并发下单，先规避掉超卖问题。
 */
@Service
public class OrderServiceImpl implements IOrderService {

    /** 订单服务要读商品信息、要扣库存，所以依赖「商品服务」而不是直接依赖商品 DAO */
    private final IPetProductService productService;
    private final OrderDao orderDao;

    public OrderServiceImpl(IPetProductService productService, OrderDao orderDao) {
        this.productService = productService;
        this.orderDao = orderDao;
    }

    @Override
    public synchronized Order checkout(List<CheckoutRequest.Item> items) {

        // ---------- 0. 购物车不能是空的 ----------
        if (items == null || items.isEmpty()) {
            throw new BusinessException("购物车是空的，无法下单");
        }

        // ---------- 1. 先把明细整理干净 ----------
        // 用 LinkedHashMap 合并「同一个商品出现多行」的情况：
        //   前端正常不会这么传，但接口不能假设调用方一定守规矩。
        LinkedHashMap<Integer, Integer> merged = new LinkedHashMap<>();
        for (CheckoutRequest.Item item : items) {
            if (item == null || item.getProductId() == null) {
                throw new BusinessException("下单明细缺少商品 id");
            }
            Integer qty = item.getQuantity();
            if (qty == null || qty <= 0) {
                throw new BusinessException("购买数量必须大于 0（商品 id=" + item.getProductId() + "）");
            }
            merged.merge(item.getProductId(), qty, Integer::sum);
        }

        // ---------- 2. 第一阶段：全部校验，只收集不修改 ----------
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map.Entry<Integer, Integer> entry : merged.entrySet()) {
            Integer productId = entry.getKey();
            int quantity = entry.getValue();

            PetProduct product = productService.getById(productId);
            if (product == null) {
                throw new BusinessException(404, "商品不存在，id=" + productId);
            }

            int stock = product.getStock() == null ? 0 : product.getStock();
            if (stock < quantity) {
                throw new BusinessException(
                        "「" + product.getName() + "」库存不足，当前仅剩 " + stock + " 件");
            }

            // 明细里抄一份「下单这一刻」的名称和单价（见 OrderItem 里的说明）
            OrderItem orderItem = new OrderItem(product.getId(), product.getName(), product.getPrice(), quantity);
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }

        // ---------- 3. 第二阶段：全部合法了，才真正扣库存 ----------
        for (OrderItem orderItem : orderItems) {
            if (!productService.decreaseStock(orderItem.getProductId(), orderItem.getQuantity())) {
                // 前面的校验刚过，正常情况下到不了这里；
                // 万一并发下被别的请求抢先扣走了，这里兜底，不让订单出错。
                throw new BusinessException("「" + orderItem.getProductName() + "」库存不足，下单失败");
            }
        }

        // ---------- 4. 生成订单并保存 ----------
        Order order = new Order(nextOrderNo(), orderItems, totalAmount, LocalDateTime.now());
        orderDao.insert(order);
        return order;
    }

    @Override
    public List<Order> listAll() {
        return orderDao.selectAll();
    }

    @Override
    public int count() {
        return orderDao.count();
    }

    /**
     * 生成订单号：PS + 下单时间 + 3 位流水号
     *
     * 例：PS20261009103015001
     * 用时间做前缀的好处是肉眼能看出下单时间，排查问题方便。
     * 真实项目里订单号一般由专门的发号器/雪花算法生成，保证全局唯一。
     */
    private String nextOrderNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "PS" + time + String.format("%03d", orderDao.count() + 1);
    }
}
