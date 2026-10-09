package com.neusoft.petshop.service.impl;

import com.neusoft.petshop.common.BusinessException;
import com.neusoft.petshop.dao.PetProductDao;
import com.neusoft.petshop.model.PetProduct;
import com.neusoft.petshop.service.IPetProductService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 商品服务实现：业务规则全部放在这一层，DAO 只负责存
 *
 * 本版相比第一版的三处关键改动：
 *
 * ① 加 @Service + 构造器注入 DAO
 *    第一版写的是 private PetProductDao productDao = new PetProductDao();
 *    自己 new 的坏处：没法被 Spring 管理，Web 层拿到的 Service 和 DAO 各是一份，
 *    而且单元测试时没法换成一个假的 DAO。
 *    改成构造器注入后，Spring 启动时把唯一的 DAO 单例塞进来，测试里也能自己传。
 *
 * ② 把「业务规则校验」抽成一个 validate 方法，控制台和 Web 两条路都走它
 *    规则只写一遍，不会出现「控制台校验了、网页没校验」的不一致。
 *
 * ③ 新增一组抛 BusinessException 的方法，旧的一组改成调它们再翻译成 1/0
 *    这样业务规则依然只有一份实现。
 */
@Service
public class PetProductServiceImpl implements IPetProductService {

    private final PetProductDao productDao;

    /**
     * 构造器注入
     *
     * 类里只有一个构造器时，Spring 4.3 之后 @Autowired 可以省略，直接写构造器即可。
     * 字段声明成 final：依赖一旦注入就不能被换掉，语义更清楚，也防止忘记赋值。
     */
    public PetProductServiceImpl(PetProductDao productDao) {
        this.productDao = productDao;
    }

    // ==================== 查询 ====================

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
        if (min != null && max != null && min.compareTo(max) > 0) {
            BigDecimal temp = min;
            min = max;
            max = temp;
        }
        return productDao.selectByPriceRange(min, max);
    }

    @Override
    public List<PetProduct> search(String keyword, String category, BigDecimal minPrice, BigDecimal maxPrice) {
        // 同样是「输反了自动交换」，两个查询入口的行为保持一致
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            BigDecimal temp = minPrice;
            minPrice = maxPrice;
            maxPrice = temp;
        }
        return productDao.selectByCondition(
                trimToNull(keyword),
                trimToNull(category),
                minPrice,
                maxPrice);
    }

    @Override
    public List<String> listCategories() {
        // LinkedHashSet 有两个好处：去重（分类不重复）＋ 保持先后顺序（前端按钮顺序稳定）
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (PetProduct p : productDao.selectAll()) {
            if (p.getCategory() != null && !p.getCategory().isEmpty()) {
                set.add(p.getCategory());
            }
        }
        return new ArrayList<>(set);
    }

    // ==================== 新增 ====================

    @Override
    public PetProduct createProduct(PetProduct product) {
        if (product == null) {
            throw new BusinessException("商品数据不能为空");
        }
        validate(product);

        if (product.getId() == null) {
            // 前端不传 id：由后端分配一个没用过的（真实项目里这是数据库自增主键的活）
            product.setId(productDao.nextId());
        } else if (productDao.selectById(product.getId()) != null) {
            throw new BusinessException("商品 id 已存在：" + product.getId());
        }

        productDao.insert(product);
        return product;
    }

    /**
     * 控制台沿用的新增：把异常翻译成返回值
     *
     * 控制台只判断 result == 1 / 0，再打印自己的提示语，
     * 所以这里把 BusinessException 吃掉换成 0，保持第一版的行为不变。
     */
    @Override
    public int addProduct(PetProduct product) {
        try {
            createProduct(product);
            return 1;
        } catch (BusinessException e) {
            return 0;
        }
    }

    // ==================== 修改 ====================

    @Override
    public PetProduct modifyProduct(PetProduct product) {
        if (product == null || product.getId() == null) {
            throw new BusinessException("缺少商品 id，无法修改");
        }
        if (productDao.selectById(product.getId()) == null) {
            throw new BusinessException(404, "商品不存在，id=" + product.getId());
        }
        validate(product);

        productDao.update(product);
        // 改完再查一次返回，保证返回给前端的是「数据库里真实的样子」
        return productDao.selectById(product.getId());
    }

    @Override
    public int updateProduct(PetProduct product) {
        try {
            modifyProduct(product);
            return 1;
        } catch (BusinessException e) {
            return 0;
        }
    }

    // ==================== 删除 ====================

    @Override
    public void removeProduct(Integer id) {
        if (id == null) {
            throw new BusinessException("缺少商品 id，无法删除");
        }
        if (productDao.deleteById(id) == 0) {
            throw new BusinessException(404, "商品不存在，id=" + id);
        }
    }

    @Override
    public int deleteProduct(Integer id) {
        try {
            removeProduct(id);
            return 1;
        } catch (BusinessException e) {
            return 0;
        }
    }

    // ==================== 库存 ====================

    @Override
    public boolean decreaseStock(Integer productId, int quantity) {
        if (productId == null || quantity <= 0) {
            return false;
        }
        return productDao.decreaseStock(productId, quantity) == 1;
    }

    // ==================== 私有工具方法 ====================

    /**
     * 商品数据校验：控制台和 Web 共用这一份规则
     *
     * 校验不通过一律抛 BusinessException，异常消息直接面向用户，
     * 所以写的是人话（「商品价格必须大于 0」），而不是「price invalid」。
     */
    private void validate(PetProduct product) {
        if (isBlank(product.getName())) {
            throw new BusinessException("商品名称不能为空");
        }
        if (product.getName().trim().length() > 50) {
            throw new BusinessException("商品名称不能超过 50 个字");
        }
        if (isBlank(product.getCategory())) {
            throw new BusinessException("商品分类不能为空");
        }
        if (product.getCategory().trim().length() > 20) {
            throw new BusinessException("商品分类不能超过 20 个字");
        }
        if (product.getPrice() == null) {
            throw new BusinessException("商品价格不能为空");
        }
        if (product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("商品价格必须大于 0");
        }
        if (product.getPrice().compareTo(new BigDecimal("999999")) > 0) {
            throw new BusinessException("商品价格不能超过 999999 元");
        }
        if (product.getStock() == null) {
            throw new BusinessException("商品库存不能为空");
        }
        if (product.getStock() < 0) {
            throw new BusinessException("商品库存不能为负数");
        }
        // 入库前统一去掉首尾空格，避免出现 "皇家猫粮 " 和 "皇家猫粮" 被当成两个商品
        product.setName(product.getName().trim());
        product.setCategory(product.getCategory().trim());
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** 把只含空白的字符串统一当成 null，好让 DAO 把它理解为「这个条件不限制」 */
    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
