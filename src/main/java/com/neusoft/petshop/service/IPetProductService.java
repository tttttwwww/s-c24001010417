package com.neusoft.petshop.service;

import com.neusoft.petshop.model.PetProduct;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品服务接口：Controller 只依赖本接口，不绑死实现类
 */
public interface IPetProductService {

    List<PetProduct> listAll();

    PetProduct getById(Integer id);

    List<PetProduct> searchByName(String name);

    List<PetProduct> listByCategory(String category);

    List<PetProduct> listByPriceRange(BigDecimal min, BigDecimal max);

    int addProduct(PetProduct product);

    int updateProduct(PetProduct product);

    int deleteProduct(Integer id);
}
