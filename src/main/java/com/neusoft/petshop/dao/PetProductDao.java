package com.neusoft.petshop.dao;

import com.neusoft.petshop.model.PetProduct;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 商品数据访问层：用 List 模拟数据库
 */
public class PetProductDao {

    private List<PetProduct> productList = InitData.init();

    /** 查询全部 */
    public List<PetProduct> selectAll() {
        return this.productList;
    }

    /** 按 id 查一条；找不到返回 null */
    public PetProduct selectById(Integer id) {
        for (PetProduct p : productList) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    /** 按名称模糊查询（名称包含即命中） */
    public List<PetProduct> selectByName(String name) {
        List<PetProduct> list = new ArrayList<>();
        for (PetProduct p : productList) {
            if (p.getName().contains(name)) {
                list.add(p);
            }
        }
        return list;
    }

    /** 按分类精确查询 */
    public List<PetProduct> selectByCategory(String category) {
        List<PetProduct> list = new ArrayList<>();
        for (PetProduct p : productList) {
            if (p.getCategory().equals(category)) {
                list.add(p);
            }
        }
        return list;
    }

    /** 按价格区间查询（含端点） */
    public List<PetProduct> selectByPriceRange(BigDecimal min, BigDecimal max) {
        List<PetProduct> list = new ArrayList<>();
        for (PetProduct p : productList) {
            if (p.getPrice().compareTo(min) >= 0 && p.getPrice().compareTo(max) <= 0) {
                list.add(p);
            }
        }
        return list;
    }

    /** 新增；返回 1 成功，0 失败 */
    public int insert(PetProduct product) {
        productList.add(product);
        return 1;
    }

    /** 按 id 修改名称、分类、价格、库存；成功返回 1，未找到返回 0 */
    public int update(PetProduct product) {
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

    /** 按 id 删除（Iterator 遍历删除，避免下标错乱）；成功 1，失败 0 */
    public int deleteById(Integer id) {
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
