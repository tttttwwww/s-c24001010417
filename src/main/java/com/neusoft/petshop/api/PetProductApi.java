package com.neusoft.petshop.api;

import com.neusoft.petshop.common.BusinessException;
import com.neusoft.petshop.common.Result;
import com.neusoft.petshop.model.PetProduct;
import com.neusoft.petshop.service.IPetProductService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品 REST 接口（Web 表现层）
 *
 * ★ 它和 controller/PetProductController 是什么关系？
 *   controller/PetProductController 是「控制台版」的控制器：它的调用方是 PetShopView（键盘输入），
 *   返回值是 Java 对象，由 View 负责用 System.out 打印。
 *   api/PetProductApi 是「Web 版」的控制器：它的调用方是浏览器，返回值会被 Jackson 转成 JSON。
 *
 *   两者是同一层的两种表现形式（一个面向控制台，一个面向浏览器），
 *   下面是同一套 Service 和 DAO —— 业务规则只有一份，不会出现两边规则不一致。
 *
 * ★ @RestController = @Controller + @ResponseBody
 *   意思是「本类每个方法的返回值都直接写进 HTTP 响应体」，所以返回 Result 对象，
 *   Spring 会自动用 Jackson 把它转成 JSON，不需要手动拼字符串。
 *
 * ★ @RequestMapping("/api/products") 是统一前缀，接口清单：
 *   GET    /api/products            商品列表（支持 keyword / category / minPrice / maxPrice 组合筛选）
 *   GET    /api/products/categories 全部分类（前端筛选按钮由它生成）
 *   GET    /api/products/{id}       商品详情
 *   POST   /api/products            新增商品
 *   PUT    /api/products/{id}       修改商品
 *   DELETE /api/products/{id}       删除商品
 */
@RestController
@RequestMapping("/api/products")
public class PetProductApi {

    private final IPetProductService productService;

    /** 构造器注入：见 PetProductServiceImpl 里的说明，官方推荐写法 */
    public PetProductApi(IPetProductService productService) {
        this.productService = productService;
    }

    /**
     * 商品列表 / 组合筛选
     *
     * GET /api/products?keyword=猫&category=食品&minPrice=20&maxPrice=100
     *
     * @RequestParam(required = false) 表示这几个参数都可以不传：
     *   不传 keyword 就是「不看名字」，不传 category 就是「不看分类」。
     * 所以下面这些请求都是合法的，返回结果依次变窄：
     *   /api/products
     *   /api/products?category=食品
     *   /api/products?category=食品&maxPrice=100
     */
    @GetMapping
    public Result<List<PetProduct>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        return Result.success(productService.search(keyword, category, minPrice, maxPrice));
    }

    /**
     * 全部分类
     *
     * 为什么要这个接口？第一版的前端把「食品/玩具/用品」写死在 HTML 里，
     * 一旦后台加了一个「医疗」分类，前端按钮不会自动出现，得改代码重新发版。
     * 让分类由后端返回，前端照着渲染，两边就永远一致了。
     *
     * ⚠️ 注意这个映射要写在 /{id} 前面（或者写成更具体的路径）。
     *    实际上 Spring 会优先匹配「字面量路径」，所以 /categories 不会被 /{id} 抢走。
     */
    @GetMapping("/categories")
    public Result<List<String>> categories() {
        return Result.success(productService.listCategories());
    }

    /** 商品详情；不存在时抛 404 业务异常，由 GlobalExceptionHandler 统一转成 Result */
    @GetMapping("/{id}")
    public Result<PetProduct> detail(@PathVariable Integer id) {
        PetProduct product = productService.getById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在，id=" + id);
        }
        return Result.success(product);
    }

    /**
     * 新增商品
     *
     * POST /api/products
     * { "name": "猫薄荷", "category": "玩具", "price": 29.9, "stock": 60 }
     *
     * 请求体里不用传 id：id 由后端分配（前端自己编 id 会和已有数据撞车）。
     */
    @PostMapping
    public Result<PetProduct> create(@RequestBody PetProduct product) {
        return Result.success("新增成功", productService.createProduct(product));
    }

    /**
     * 修改商品
     *
     * PUT /api/products/3
     * { "name": "逗猫棒（升级款）", "category": "玩具", "price": 22.9, "stock": 88 }
     *
     * 这是「整体更新」语义：请求体要带全 4 个业务字段。
     * id 以 URL 上的为准（product.setId(id)），这样即使请求体里写了别的 id 也覆盖不掉。
     * 这个细节很重要：否则前端一不留神就能拿着 A 商品的请求改到 B 商品头上。
     */
    @PutMapping("/{id}")
    public Result<PetProduct> update(@PathVariable Integer id, @RequestBody PetProduct product) {
        product.setId(id);
        return Result.success("修改成功", productService.modifyProduct(product));
    }

    /** 删除商品 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        productService.removeProduct(id);
        return Result.success("删除成功", null);
    }
}
