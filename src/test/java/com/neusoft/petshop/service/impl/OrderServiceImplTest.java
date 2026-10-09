package com.neusoft.petshop.service.impl;

import com.neusoft.petshop.common.BusinessException;
import com.neusoft.petshop.dao.OrderDao;
import com.neusoft.petshop.dao.PetProductDao;
import com.neusoft.petshop.dto.CheckoutRequest;
import com.neusoft.petshop.model.Order;
import com.neusoft.petshop.model.PetProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 订单服务单元测试：重点验证「两阶段校验不出错」这条最关键的规则
 *
 * 执行：mvn test
 */
@DisplayName("订单服务：下单与库存")
class OrderServiceImplTest {

    private OrderDao orderDao;
    private PetProductDao productDao;
    private PetProductServiceImpl productService;
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        productDao = new PetProductDao();
        productService = new PetProductServiceImpl(productDao);
        orderDao = new OrderDao();
        orderService = new OrderServiceImpl(productService, orderDao);
    }

    /** 造一条购物车明细 */
    private CheckoutRequest.Item item(Integer productId, Integer quantity) {
        CheckoutRequest.Item it = new CheckoutRequest.Item();
        it.setProductId(productId);
        it.setQuantity(quantity);
        return it;
    }

    @Test
    @DisplayName("正常下单：订单号、金额、明细快照都对，库存按数量扣减")
    void checkout_shouldCreateOrderAndDecreaseStock() {
        // 皇家猫粮(id=1) ¥188.00 库存 50 ×2 ；逗猫棒(id=3) ¥19.90 库存 100 ×1
        Order order = orderService.checkout(List.of(item(1, 2), item(3, 1)));

        assertNotNull(order.getOrderNo());
        assertTrue(order.getOrderNo().startsWith("PS"), "订单号应以 PS 开头");
        assertEquals(19, order.getOrderNo().length(), "PS + 14 位时间 + 3 位流水");
        assertEquals(2, order.getItems().size());

        // 188*2 + 19.90 = 395.90
        assertEquals(0, order.getTotalAmount().compareTo(new BigDecimal("395.90")));

        // 库存被扣了
        assertEquals(48, productService.getById(1).getStock());
        assertEquals(99, productService.getById(3).getStock());

        // 订单存下来了
        assertEquals(1, orderService.count());
        assertEquals(1, orderService.listAll().size());
    }

    @Test
    @DisplayName("下单后商品涨价，历史订单金额不受影响（明细快照的意义）")
    void checkout_shouldSnapshotPriceAndName() {
        Order order = orderService.checkout(List.of(item(1, 1)));
        BigDecimal paidAmount = order.getTotalAmount();
        String paidName = order.getItems().get(0).getProductName();

        // 事后把商品改名、涨价
        productService.modifyProduct(
                new PetProduct(1, "皇家猫粮（涨价版）", "食品", new BigDecimal("999.00"), 48));

        // 历史订单里还是下单那一刻的名称和金额
        assertEquals("皇家猫粮", paidName);
        assertEquals("皇家猫粮", order.getItems().get(0).getProductName());
        assertEquals(0, order.getTotalAmount().compareTo(paidAmount));
        assertEquals(0, paidAmount.compareTo(new BigDecimal("188.00")));
    }

    @Test
    @DisplayName("库存不足时必须拦住，而且一件都不能扣（两阶段校验）")
    void checkout_shouldNotDecreaseAnyStockWhenOneItemIsInsufficient() {
        int stockOfCatFoodBefore = productService.getById(1).getStock();   // 50
        int stockOfDogFoodBefore = productService.getById(2).getStock();   // 40

        // 第 1 件合法、第 2 件超出库存（宝路狗粮只有 40 件，要买 999 件）
        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.checkout(List.of(item(1, 2), item(2, 999))));

        assertTrue(e.getMessage().contains("库存不足"), "提示应说明库存不足，实际：" + e.getMessage());

        // 关键断言：第 1 件商品的库存也不能被扣 —— 边校验边扣就会在这里翻车
        assertEquals(stockOfCatFoodBefore, productService.getById(1).getStock());
        assertEquals(stockOfDogFoodBefore, productService.getById(2).getStock());
        assertEquals(0, orderService.count(), "失败的下单不该产生订单");
    }

    @Test
    @DisplayName("购物车为空 / 明细缺少 id 都要拦住")
    void checkout_shouldRejectEmptyCart() {
        assertThrows(BusinessException.class, () -> orderService.checkout(null));
        assertThrows(BusinessException.class, () -> orderService.checkout(List.of()));
        assertThrows(BusinessException.class, () -> orderService.checkout(List.of(item(null, 1))));
    }

    @Test
    @DisplayName("购买数量必须大于 0")
    void checkout_shouldRejectIllegalQuantity() {
        assertThrows(BusinessException.class, () -> orderService.checkout(List.of(item(1, 0))));
        assertThrows(BusinessException.class, () -> orderService.checkout(List.of(item(1, -2))));
        assertThrows(BusinessException.class, () -> orderService.checkout(List.of(item(1, null))));
    }

    @Test
    @DisplayName("商品不存在时返回 404 业务异常")
    void checkout_shouldThrow404WhenProductMissing() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.checkout(List.of(item(999, 1))));
        assertEquals(404, e.getCode());
    }

    @Test
    @DisplayName("同一个商品传成两行时自动合并数量，库存按总量扣")
    void checkout_shouldMergeDuplicateLines() {
        Order order = orderService.checkout(List.of(item(1, 2), item(1, 3)));

        assertEquals(1, order.getItems().size(), "合并后只剩一行");
        assertEquals(5, order.getItems().get(0).getQuantity());
        assertEquals(0, order.getTotalAmount().compareTo(new BigDecimal("940.00")));  // 188 × 5
        assertEquals(45, productService.getById(1).getStock());                       // 50 - 5
    }

    @Test
    @DisplayName("多次下单，库存累减，订单号不重复")
    void checkout_shouldAccumulateStockAndKeepOrderNoUnique() {
        Order first = orderService.checkout(List.of(item(1, 1)));
        Order second = orderService.checkout(List.of(item(1, 1)));

        assertEquals(2, orderService.count());
        assertEquals(48, productService.getById(1).getStock());

        // 流水号不同，所以订单号不同
        assertTrue(!first.getOrderNo().equals(second.getOrderNo()),
                "订单号不该重复：" + first.getOrderNo());
    }
}
