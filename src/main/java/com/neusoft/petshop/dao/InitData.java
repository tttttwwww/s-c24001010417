package com.neusoft.petshop.dao;

import com.neusoft.petshop.model.PetProduct;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 初始化测试数据（模拟「数据库」里已有的商品）
 * 分类：食品 / 玩具 / 用品
 */
public class InitData {

    public static List<PetProduct> init() {
        List<PetProduct> list = new ArrayList<>();
        list.add(new PetProduct(1, "皇家猫粮", "食品", new BigDecimal("188.00"), 50));
        list.add(new PetProduct(2, "宝路狗粮", "食品", new BigDecimal("128.00"), 40));
        list.add(new PetProduct(3, "逗猫棒", "玩具", new BigDecimal("19.90"), 100));
        list.add(new PetProduct(4, "狗咬胶", "玩具", new BigDecimal("25.00"), 80));
        list.add(new PetProduct(5, "猫砂盆", "用品", new BigDecimal("59.00"), 30));
        list.add(new PetProduct(6, "狗窝", "用品", new BigDecimal("89.00"), 25));
        list.add(new PetProduct(7, "宠物饮水器", "用品", new BigDecimal("45.00"), 35));
        list.add(new PetProduct(8, "小鱼干零食", "食品", new BigDecimal("15.90"), 120));
        return list;
    }
}
