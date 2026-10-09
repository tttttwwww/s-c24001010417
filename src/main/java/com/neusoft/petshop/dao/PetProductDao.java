package com.neusoft.petshop.dao;

import com.neusoft.petshop.model.PetProduct;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 商品数据访问层：用 List 模拟数据库
 *
 * 本版相比第一版做了 4 处改造，都是为了「能被 Web 层安全地共享」：
 *
 * ① 加 @Repository：交给 Spring 管理，全局只有一个实例（单例 Bean）。
 *    如果还像第一版那样每次 new 一个，每个 DAO 各持一份 List，
 *    浏览器刷新一次看到的数据可能就对不上了。
 *
 * ② 方法加 synchronized：Tomcat 处理请求是多线程的，
 *    同时来两个请求（比如一边下单一边列表）去改同一个 ArrayList 会出问题。
 *    synchronized 让同一时刻只有一个线程能进这些方法，简单可靠。
 *
 * ③ selectAll() 返回副本，不再直接返回内部 List：
 *    否则调用方拿到引用后随手 add/clear，就把「数据库」改了。
 *
 * ④ 新增 nextId() 和 decreaseStock()：
 *    nextId 给 Web 新增商品用（前端不用自己编 id）；
 *    decreaseStock 给下单扣库存用，扣不动就返回 0。
 */
@Repository
public class PetProductDao {

    /** 模拟的「商品表」。加了 Spring 之后它是单例里的一份数据，应用重启就复原 */
    private final List<PetProduct> productList = new ArrayList<>(InitData.init());

    /** 查询全部（返回副本，外面改不动内部数据） */
    public synchronized List<PetProduct> selectAll() {
        return new ArrayList<>(productList);
    }

    /** 按 id 查一条；找不到返回 null */
    public synchronized PetProduct selectById(Integer id) {
        if (id == null) {
            return null;
        }
        for (PetProduct p : productList) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    /**
     * 查询 + 组合筛选（一个方法顶替原来四个查询方法）
     *
     * 为什么合并：原来 selectByName / selectByCategory / selectByPriceRange 各写一遍循环，
     * 而 Web 端的筛选条件是「可选的、能同时生效的」（比如「食品分类里 20~100 元的、名字带猫的」）。
     * 合并成一个方法后，null 就代表「这个条件不限制」，一套循环搞定。
     *
     * @param keyword  名称模糊关键字，null 或空串表示不限制
     * @param category 分类精确匹配，null 或空串表示不限制
     * @param min      最低价（含），null 表示不限制
     * @param max      最高价（含），null 表示不限制
     */
    public synchronized List<PetProduct> selectByCondition(String keyword, String category,
                                                           BigDecimal min, BigDecimal max) {
        List<PetProduct> list = new ArrayList<>();
        for (PetProduct p : productList) {
            if (keyword != null && !keyword.isEmpty() && !p.getName().contains(keyword)) {
                continue;
            }
            if (category != null && !category.isEmpty() && !p.getCategory().equals(category)) {
                continue;
            }
            if (min != null && p.getPrice().compareTo(min) < 0) {
                continue;
            }
            if (max != null && p.getPrice().compareTo(max) > 0) {
                continue;
            }
            list.add(p);
        }
        return list;
    }

    /** 按名称模糊查询（保留第一版的方法，控制台路径仍在用） */
    public synchronized List<PetProduct> selectByName(String name) {
        return selectByCondition(name, null, null, null);
    }

    /** 按分类精确查询（保留第一版的方法） */
    public synchronized List<PetProduct> selectByCategory(String category) {
        return selectByCondition(null, category, null, null);
    }

    /** 按价格区间查询，含端点（保留第一版的方法） */
    public synchronized List<PetProduct> selectByPriceRange(BigDecimal min, BigDecimal max) {
        return selectByCondition(null, null, min, max);
    }

    /**
     * 取一个还没被用过的 id：当前最大 id + 1
     *
     * Web 端新增商品时前端不传 id，由后端分配，避免两个人同时提交同一个 id。
     * 真实项目里这件事交给数据库的自增主键，这里用 List 模拟就得自己算。
     */
    public synchronized Integer nextId() {
        int max = 0;
        for (PetProduct p : productList) {
            if (p.getId() != null && p.getId() > max) {
                max = p.getId();
            }
        }
        return max + 1;
    }

    /** 新增；返回 1 成功，0 失败 */
    public synchronized int insert(PetProduct product) {
        productList.add(product);
        return 1;
    }

    /** 按 id 修改名称、分类、价格、库存；成功返回 1，未找到返回 0 */
    public synchronized int update(PetProduct product) {
        for (PetProduct p : productList) {
            if (p.getId().equals(product.getId())) {
                p.setName(product.getName());
                p.setCategory(product.getCategory());
                p.setPrice(product.getPrice());
                p.setStock(product.getStock());
                return 1;
            }
        }
        return 0;
    }

    /**
     * 下单扣库存
     *
     * 三件事必须一起判断，少一个都会出错：
     *   商品存在吗？扣的数量合法吗？库存够吗？
     * 返回值 1 表示扣成功，0 表示没扣（调用方负责抛出具体原因）。
     *
     * 注意这个方法必须是「读 + 判断 + 写」一整个原子操作，
     * 所以整个方法 synchronized —— 否则两个请求同时判断「库存够」，
     * 就会把同一件商品卖出两次（经典的超卖问题）。
     */
    public synchronized int decreaseStock(Integer id, int quantity) {
        if (quantity <= 0) {
            return 0;
        }
        PetProduct p = selectById(id);
        if (p == null || p.getStock() == null || p.getStock() < quantity) {
            return 0;
        }
        p.setStock(p.getStock() - quantity);
        return 1;
    }

    /** 按 id 删除（Iterator 遍历删除，避免下标错乱）；成功 1，失败 0 */
    public synchronized int deleteById(Integer id) {
        int flag = 0;
        Iterator<PetProduct> it = productList.iterator();
        while (it.hasNext()) {
            PetProduct p = it.next();
            if (p.getId().equals(id)) {
                it.remove();
                flag = 1;
                break;
            }
        }
        return flag;
    }
}
