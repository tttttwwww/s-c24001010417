package com.neusoft.petshop.dao;

import com.neusoft.petshop.model.Order;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单数据访问层：和 PetProductDao 一样，用 List 模拟数据库
 *
 * 有了这个类，订单相关的读写就走在了和商品一样的分层路径上：
 *   OrderApi → IOrderService → OrderDao → Order
 * 而不是让 Service 自己去 new 一个 List 存数据 —— 那就等于把 DAO 层的活偷偷干在 Service 里，
 * 分层架构就白学了。
 */
@Repository
public class OrderDao {

    /** 模拟的「订单表」。单例 Bean，应用重启复原 */
    private final List<Order> orderList = new ArrayList<>();

    /** 保存一条订单，返回订单号 */
    public synchronized String insert(Order order) {
        orderList.add(order);
        return order.getOrderNo();
    }

    /** 查询全部订单（最新的排前面，返回副本） */
    public synchronized List<Order> selectAll() {
        List<Order> copy = new ArrayList<>(orderList);
        copy.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
        return copy;
    }

    /** 订单总数 */
    public synchronized int count() {
        return orderList.size();
    }
}
