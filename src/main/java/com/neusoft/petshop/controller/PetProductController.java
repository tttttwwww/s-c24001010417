package com.neusoft.petshop.controller;

import com.neusoft.petshop.dao.PetProductDao;
import com.neusoft.petshop.model.PetProduct;
import com.neusoft.petshop.service.IPetProductService;
import com.neusoft.petshop.service.impl.PetProductServiceImpl;

import java.math.BigDecimal;
import java.util.List;

/**
 * 控制器（控制台版）：接收 View 的调用，转调 Service
 *
 * 注意它和 api/PetProductApi 的分工：
 *   本类   调用方是 PetShopView（键盘输入 / System.out 输出）
 *   Api 类 调用方是浏览器（JSON 输入 / JSON 输出）
 * 两者都在「控制层」，共用下面同一套 Service 和 DAO。
 */
public class PetProductController {

    /**
     * 控制台版手动组装依赖：new 出 DAO，再塞给 Service 实现
     *
     * 为什么这里不用 Spring 注入？
     * 因为这个类服务于控制台入口 PetShopView，而 PetShopView 是独立的 main 方法，
     * 不启动 Spring 容器。Web 那边的依赖注入由 api/PetProductApi + @Service 完成。
     * 两种组装方式指向的是同一套类，只是「谁来把对象接起来」不同。
     *
     * 本版改动最小的一处：因为 PetProductServiceImpl 改成了构造器注入 DAO，
     * 所以这里的 new 要跟着补上 DAO 参数。
     */
    private final IPetProductService productService = new PetProductServiceImpl(new PetProductDao());

    public List<PetProduct> listAll() {
        return productService.listAll();
    }

    public PetProduct getById(Integer id) {
        return productService.getById(id);
    }

    public List<PetProduct> searchByName(String name) {
        return productService.searchByName(name);
    }

    public List<PetProduct> listByCategory(String category) {
        return productService.listByCategory(category);
    }

    public List<PetProduct> listByPriceRange(BigDecimal min, BigDecimal max) {
        return productService.listByPriceRange(min, max);
    }

    public int addProduct(PetProduct product) {
        return productService.addProduct(product);
    }

    public int updateProduct(PetProduct product) {
        return productService.updateProduct(product);
    }

    public int deleteProduct(Integer id) {
        return productService.deleteProduct(id);
    }
}
