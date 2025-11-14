package cn.aspes.agri.trade.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类，用于CORS跨域配置
 * 
 * 在开发环境中，允许所有来源的请求。
 * 在生产环境中，应该替换allowedOrigins中的值为前端应用的实际域名。
 * 
 * 示例：
 * 开发环境: .allowedOrigins("*")
 * 生产环境: .allowedOrigins("https://yourdomain.com", "https://www.yourdomain.com")
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置CORS跨域
     *
     * @param registry CORS注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // 仅对API路径进行CORS配置
                .allowedOrigins(
                        "http://localhost:3000",  // React开发服务器
                        "http://localhost:3001",  // 前端应用
                        "http://localhost:5173",  // Vite开发服务器
                        "http://127.0.0.1:3000",
                        "http://127.0.0.1:3001",
                        "http://127.0.0.1:5173"
                ) // 生产环境应替换为特定域名
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // 预检请求结果缓存时间，单位：秒
        
        // 对Swagger相关路径也允许跨域
        registry.addMapping("/swagger-ui/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "OPTIONS")
                .maxAge(3600);
        
        registry.addMapping("/v3/api-docs/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "OPTIONS")
                .maxAge(3600);
    }
}