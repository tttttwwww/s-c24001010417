package com.neusoft.petshop.controller;

import com.neusoft.petshop.model.PetProduct;
import com.neusoft.petshop.service.IPetProductService;
import com.neusoft.petshop.service.impl.PetProductServiceImpl;

import java.math.BigDecimal;
import java.util.List;

/**
 * 控制器：接收 View 的调用，转调 Service
 */
public class PetProductController {

    private IPetProductService productService = new PetProductServiceImpl();

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
