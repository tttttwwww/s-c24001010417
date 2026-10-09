package com.neusoft.petshop.service.impl;

import com.neusoft.petshop.common.BusinessException;
import com.neusoft.petshop.dao.PetProductDao;
import com.neusoft.petshop.model.PetProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 商品服务单元测试
 *
 * ★ 为什么这些测试不需要启动 Spring？
 *   因为 PetProductServiceImpl 的依赖（PetProductDao）是从构造器传进来的，
 *   测试里自己 new 一个真的 DAO 塞进去就行 —— 不需要容器、快、稳定。
 *   这正是「构造器注入比字段注入好测试」的具体体现：
 *   如果 Service 里写死 private PetProductDao dao = new PetProductDao()，
 *   你就没有任何办法在测试里替换它。
 *
 * ★ DAO 用的是内存 List，而且 InitData.init() 每次都返回一份全新的数据，
 *   所以每个测试方法 new 一个 DAO 就等于拿到一个干净的「数据库」，测试之间互不干扰。
 *
 * 执行：mvn test
 */
@DisplayName("商品服务：业务规则")
class PetProductServiceImplTest {

    private PetProductDao dao;
    private PetProductServiceImpl service;

    @BeforeEach
    void setUp() {
        dao = new PetProductDao();
        service = new PetProductServiceImpl(dao);
    }

    /** 造一个合法的商品，各用例只改自己关心的那个字段 */
    private PetProduct product(String name, String category, String price, int stock) {
        return new PetProduct(null, name, category, new BigDecimal(price), stock);
    }

    // ==================== 查询 ====================

    @Test
    @DisplayName("初始数据是 8 件商品")
    void listAll_shouldReturnSeedData() {
        assertEquals(8, service.listAll().size());
    }

    @Test
    @DisplayName("组合查询：分类 + 价格区间同时生效")
    void search_shouldCombineAllConditions() {
        List<PetProduct> list = service.search(null, "食品", new BigDecimal("20"), new BigDecimal("200"));
        // 种子数据里「食品」有：皇家猫粮 188、宝路狗粮 128、小鱼干零食 15.9
        // 20~200 之间只剩前两个
        assertEquals(2, list.size());
        assertTrue(list.stream().allMatch(p -> "食品".equals(p.getCategory())));
    }

    @Test
    @DisplayName("组合查询：关键字模糊匹配")
    void search_shouldMatchKeyword() {
        List<PetProduct> list = service.search("猫", null, null, null);
        // 皇家猫粮、逗猫棒、猫砂盆
        assertEquals(3, list.size());
    }

    @Test
    @DisplayName("价格区间输反时自动交换（向后兼容第一版的业务规则）")
    void search_shouldSwapReversedPriceRange() {
        List<PetProduct> list = service.search(null, null, new BigDecimal("100"), new BigDecimal("20"));
        assertEquals(4, list.size());
    }

    @Test
    @DisplayName("分类列表去重且保持顺序")
    void listCategories_shouldBeDistinct() {
        List<String> categories = service.listCategories();
        assertEquals(3, categories.size());
        assertEquals(List.of("食品", "玩具", "用品"), categories);
    }

    // ==================== 新增 ====================

    @Test
    @DisplayName("新增：不传 id 时自动分配一个没用过的 id")
    void createProduct_shouldAssignIdWhenNull() {
        PetProduct created = service.createProduct(product("猫薄荷", "玩具", "29.90", 60));

        assertNotNull(created.getId());
        assertEquals(9, created.getId());          // 种子数据最大 id 是 8
        assertEquals(9, service.listAll().size());
    }

    @Test
    @DisplayName("新增：id 重复时抛业务异常")
    void createProduct_shouldRejectDuplicateId() {
        PetProduct p = product("重复id", "玩具", "10.00", 5);
        p.setId(1);

        BusinessException e = assertThrows(BusinessException.class, () -> service.createProduct(p));
        assertTrue(e.getMessage().contains("已存在"));
    }

    @Test
    @DisplayName("新增：名称为空 / 全空格都要被拦住")
    void createProduct_shouldRejectBlankName() {
        assertThrows(BusinessException.class, () -> service.createProduct(product(null, "玩具", "10.00", 5)));
        assertThrows(BusinessException.class, () -> service.createProduct(product("   ", "玩具", "10.00", 5)));
    }

    @Test
    @DisplayName("新增：价格必须大于 0，库存不能为负")
    void createProduct_shouldRejectIllegalNumber() {
        assertThrows(BusinessException.class, () -> service.createProduct(product("零元购", "玩具", "0", 5)));
        assertThrows(BusinessException.class, () -> service.createProduct(product("负数价", "玩具", "-1", 5)));
        assertThrows(BusinessException.class, () -> service.createProduct(product("负库存", "玩具", "10.00", -5)));
    }

    @Test
    @DisplayName("新增：名称首尾空格会被去掉，避免「皇家猫粮 」和「皇家猫粮」变成两个商品")
    void createProduct_shouldTrimName() {
        PetProduct created = service.createProduct(product("  猫薄荷  ", "  玩具  ", "29.90", 60));
        assertEquals("猫薄荷", created.getName());
        assertEquals("玩具", created.getCategory());
    }

    // ==================== 修改 ====================

    @Test
    @DisplayName("修改：商品不存在时抛 404 业务异常")
    void modifyProduct_shouldThrow404WhenMissing() {
        PetProduct p = product("不存在", "玩具", "10.00", 1);
        p.setId(999);

        BusinessException e = assertThrows(BusinessException.class, () -> service.modifyProduct(p));
        assertEquals(404, e.getCode());
    }

    @Test
    @DisplayName("修改：正常改掉价格和库存")
    void modifyProduct_shouldUpdateFields() {
        PetProduct p = product("逗猫棒（升级款）", "玩具", "22.90", 88);
        p.setId(3);

        PetProduct updated = service.modifyProduct(p);

        assertEquals("逗猫棒（升级款）", updated.getName());
        assertEquals(0, updated.getPrice().compareTo(new BigDecimal("22.90")));
        assertEquals(88, updated.getStock());
    }

    // ==================== 删除 ====================

    @Test
    @DisplayName("删除：删掉后查不到，条数减一")
    void removeProduct_shouldDelete() {
        service.removeProduct(1);

        assertNull(service.getById(1));
        assertEquals(7, service.listAll().size());
    }

    @Test
    @DisplayName("删除：商品不存在时抛 404 业务异常")
    void removeProduct_shouldThrow404WhenMissing() {
        BusinessException e = assertThrows(BusinessException.class, () -> service.removeProduct(999));
        assertEquals(404, e.getCode());
    }

    // ==================== 库存 ====================

    @Test
    @DisplayName("扣库存：够就扣成功，数量超了就扣不动")
    void decreaseStock_shouldCheckAvailability() {
        // 皇家猫粮库存 50，扣 3 件
        assertTrue(service.decreaseStock(1, 3));
        assertEquals(47, service.getById(1).getStock());

        // 再扣 999 件，库存不够，必须失败且库存不变
        assertFalse(service.decreaseStock(1, 999));
        assertEquals(47, service.getById(1).getStock());

        // 数量非法也扣不动
        assertFalse(service.decreaseStock(1, 0));
        assertFalse(service.decreaseStock(1, -5));
        assertFalse(service.decreaseStock(999, 1));
    }

    // ==================== 兼容旧契约 ====================

    @Test
    @DisplayName("控制台契约不变：addProduct 成功返回 1、失败返回 0")
    void addProduct_shouldKeepLegacyReturnValue() {
        assertEquals(1, service.addProduct(new PetProduct(100, "新商品", "玩具", new BigDecimal("9.90"), 10)));
        // 同一个 id 再来一次 → 0
        assertEquals(0, service.addProduct(new PetProduct(100, "再来一次", "玩具", new BigDecimal("9.90"), 10)));
        // 数据不合法 → 0（不抛异常，控制台那边才好按老逻辑提示）
        assertEquals(0, service.addProduct(new PetProduct(101, "", "玩具", new BigDecimal("9.90"), 10)));
    }

    @Test
    @DisplayName("控制台契约不变：updateProduct / deleteProduct 同样返回 1 或 0")
    void updateAndDeleteProduct_shouldKeepLegacyReturnValue() {
        assertEquals(1, service.updateProduct(new PetProduct(1, "皇家猫粮（新）", "食品", new BigDecimal("199.00"), 50)));
        assertEquals(0, service.updateProduct(new PetProduct(999, "不存在", "食品", new BigDecimal("1.00"), 1)));

        assertEquals(1, service.deleteProduct(1));
        assertEquals(0, service.deleteProduct(1));
    }
}
