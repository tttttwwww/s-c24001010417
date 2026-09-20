package com.neusoft.petshop.service.impl;

import com.neusoft.petshop.dao.PetProductDao;
import com.neusoft.petshop.model.PetProduct;
import com.neusoft.petshop.service.IPetProductService;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品服务实现：业务规则放在这一层，DAO 只负责存
 */
public class PetProductServiceImpl implements IPetProductService {

    private PetProductDao productDao = new PetProductDao();

    @Override
    public List<PetProduct> listAll() {
        return productDao.selectAll();
    }

    @Override
    public PetProduct getById(Integer id) {
        return productDao.selectById(id);
    }

    @Override
    public List<PetProduct> searchByName(String name) {
        return productDao.selectByName(name);
    }

    @Override
    public List<PetProduct> listByCategory(String category) {
        return productDao.selectByCategory(category);
    }

    @Override
    public List<PetProduct> listByPriceRange(BigDecimal min, BigDecimal max) {
        // 业务规则：最低价 > 最高价时自动交换，避免用户输反
        if (min.compareTo(max) > 0) {
            BigDecimal temp = min;
            min = max;
            max = temp;
        }
        return productDao.selectByPriceRange(min, max);
    }

    @Override
    public int addProduct(PetProduct product) {
        // 业务规则：id 不能重复，先查再插
        if (productDao.selectById(product.getId()) != null) {
            return 0;
        }
        return productDao.insert(product);
    }

    @Override
    public int updateProduct(PetProduct product) {
        return productDao.update(product);
    }

    @Override
    public int deleteProduct(Integer id) {
        return productDao.deleteById(id);
    }
}
