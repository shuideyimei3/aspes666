package cn.aspes.agri.trade.config;

import cn.aspes.agri.trade.filter.JwtAuthenticationFilter;
import cn.aspes.agri.trade.service.StatisticsService;
import cn.aspes.agri.trade.util.JwtUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 测试配置类
 * 
 * 提供测试环境所需的安全配置和Bean定义
 */
@TestConfiguration
@EnableWebSecurity
@Profile("test")
public class TestConfig {
    
    /**
     * 测试环境安全配置
     * 
     * 允许所有请求通过，便于单元测试
     */
    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .headers(headers -> headers.frameOptions().disable());
        return http.build();
    }
    
    /**
     * 创建JwtProperties Bean用于测试
     */
    @Bean
    @Primary
    public JwtProperties jwtProperties() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("testSecretKeyForUnitTestingThatIsLongEnough");
        jwtProperties.setExpiration(86400000L); // 24小时
        return jwtProperties;
    }
}