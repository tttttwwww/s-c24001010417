package com.neusoft.petshop.service;

import com.neusoft.petshop.model.PetProduct;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品服务接口：Controller / Api 都只依赖本接口，不绑死实现类
 *
 * 接口里分成两组方法，是老代码平滑升级的结果：
 *
 * 【第一组】原来控制台用的 8 个方法，返回 int（1 成功 / 0 失败），签名一个字没改。
 *          为什么不改？改了 view/PetShopView 和 controller/PetProductController 就得跟着大改，
 *          而它们本来是好用的。让旧代码继续按旧约定工作，是升级时最省事也最安全的做法。
 *
 * 【第二组】Web 用的新方法，规则不满足时抛 BusinessException，异常里带中文提示。
 *          为什么要有这一组？因为浏览器需要一个「为什么失败」的说明，
 *          只回一个 0，前端只能弹出「操作失败」，用户完全不知道哪填错了。
 *
 * 两组方法共用同一套业务规则实现（见 PetProductServiceImpl），不是两套逻辑。
 */
public interface IPetProductService {

    // ==================== 第一组：控制台沿用的方法 ====================

    /** 查询全部商品 */
    List<PetProduct> listAll();

    /** 按 id 查询；找不到返回 null */
    PetProduct getById(Integer id);

    /** 按名称模糊查询 */
    List<PetProduct> searchByName(String name);

    /** 按分类精确查询 */
    List<PetProduct> listByCategory(String category);

    /** 按价格区间查询（含端点）；最低价大于最高价时自动交换 */
    List<PetProduct> listByPriceRange(BigDecimal min, BigDecimal max);

    /** 新增商品；返回 1 成功，0 失败（id 重复或数据不合法） */
    int addProduct(PetProduct product);

    /** 修改商品；返回 1 成功，0 失败（商品不存在或数据不合法） */
    int updateProduct(PetProduct product);

    /** 删除商品；返回 1 成功，0 失败（商品不存在） */
    int deleteProduct(Integer id);

    // ==================== 第二组：Web 新增的方法 ====================

    /**
     * 组合条件查询（Web 端商品列表用）
     *
     * 四个条件都是可选的：传 null 就表示这一项不限制。
     * 对应接口：GET /api/products?keyword=猫&category=食品&minPrice=20&maxPrice=100
     */
    List<PetProduct> search(String keyword, String category, BigDecimal minPrice, BigDecimal maxPrice);

    /** 取所有已存在的分类（前端筛选按钮由它生成，避免前端硬编码写死） */
    List<String> listCategories();

    /**
     * 新增商品（Web 端用）
     *
     * 和前一组 addProduct 的区别：
     *   id 为 null 时自动分配一个没用过的 id（前端不用自己编 id）；
     *   数据不合法时抛 BusinessException，消息直接可以给用户看。
     */
    PetProduct createProduct(PetProduct product);

    /** 修改商品（Web 端用）；商品不存在抛 404 业务异常 */
    PetProduct modifyProduct(PetProduct product);

    /** 删除商品（Web 端用）；商品不存在抛 404 业务异常 */
    void removeProduct(Integer id);

    /**
     * 扣减库存（给订单服务调用）
     *
     * 库存不足、商品不存在、数量非法都返回 false。
     * 为什么不让 OrderService 自己去查库存再改？
     * 因为「查够不够 + 扣掉」必须是同一个原子操作，否则并发下会超卖，
     * 这个能力只能由持有数据的商品服务提供。
     */
    boolean decreaseStock(Integer productId, int quantity);
}
