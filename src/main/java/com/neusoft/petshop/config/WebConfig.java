package com.neusoft.petshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 相关配置：跨域（CORS）
 *
 * ★ 什么是跨域？
 *   浏览器有个安全策略叫「同源策略」：页面的地址和接口的地址
 *   只要「协议 + 域名 + 端口」有一个不同，就算跨域，浏览器会拦住这个请求。
 *
 *   本项目正常用法（用 http://localhost:8080/ 打开页面）其实不跨域，
 *   因为页面和接口是同一个服务。
 *
 * ★ 那为什么还要配？
 *   因为同学调试时很可能直接双击 index.html 打开（此时页面地址是 file://），
 *   从 file:// 调 http://localhost:8080/api/... 就是跨域，会被浏览器拦掉，
 *   表现是「页面能显示但商品列表一直是空的」，控制台报 CORS 错误。
 *   配上这个之后，两种打开方式都能用，少一类「明明接口没错却调不通」的困惑。
 *
 * ★ allowedOriginPatterns("*") 在学习项目里没问题；
 *   真实上线必须写清楚只允许哪几个域名，否则等于把接口开放给任何网站调用。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
