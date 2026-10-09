package com.neusoft.petshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 宠物用品销售网站 —— Web 版启动类
 *
 * @SpringBootApplication 是组合注解：
 *   @SpringBootConfiguration  声明这是配置类
 *   @EnableAutoConfiguration  按 classpath 里的依赖自动装配（引了 starter-web 就自动配 Tomcat 和 Spring MVC）
 *   @ComponentScan            扫描当前包及子包（所以 @RestController / @Service / @Repository 必须放在
 *                             com.neusoft.petshop 下面，放到别的根包会「启动成功但接口 404」）
 *
 * 启动后：
 *   商城首页   http://localhost:8080/                 （静态页 src/main/resources/static/index.html）
 *   商品接口   http://localhost:8080/api/products
 *   订单接口   http://localhost:8080/api/orders
 *
 * 注意：原来的控制台入口 com.neusoft.petshop.view.PetShopView 依然保留，它是独立的 main 方法，
 * 用 mvn exec:java 单独启动，不经过这里。
 */
@SpringBootApplication
public class PetShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetShopApplication.class, args);
        System.out.println("""
                ==========================================
                   萌宠用品小店（Web 版）启动成功！
                   商城首页: http://localhost:8080/
                   商品接口: http://localhost:8080/api/products
                   订单接口: http://localhost:8080/api/orders
                   控制台版: mvn compile exec:java
                ==========================================
                """);
    }
}
