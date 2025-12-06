package cn.aspes.agri.trade.filter;

import cn.aspes.agri.trade.enums.UserRole;
import cn.aspes.agri.trade.security.CustomUserDetails;
import cn.aspes.agri.trade.util.JwtUtil;
import cn.aspes.agri.trade.service.StatisticsService;
import cn.hutool.core.util.StrUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT认证过滤器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final StatisticsService statisticsService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String TOKEN_PREFIX = "token:";
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        String token = getTokenFromRequest(request);
        
        // 验证Token是否过期和是否在Redis中有效
        if (StrUtil.isNotBlank(token) && !jwtUtil.isTokenExpired(token) && isTokenValid(token)) {
            try {
                Claims claims = jwtUtil.parseToken(token);
                Long userId = Long.parseLong(claims.getSubject());
                String username = claims.get("username", String.class);
                String roleCode = claims.get("role", String.class);
                UserRole role = UserRole.valueOf(roleCode.toUpperCase());
                
                // 创建CustomUserDetails对象，包含所有必要信息
                CustomUserDetails userDetails = new CustomUserDetails(
                        userId, username, null, role, true);
                
                // 创建认证对象，使用CustomUserDetails对象作为principal
                // 这样既支持@AuthenticationPrincipal注解，也能通过getDetails()获取用户信息
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(userDetails);
                
                // 设置到安全上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
                // 记录农户请求活跃事件（市级）
                if (role == UserRole.FARMER) {
                    statisticsService.recordFarmerActivity(userId);
                }
            } catch (Exception e) {
                logger.error("JWT认证失败", e);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 检查Token是否在Redis中有效
     */
    private boolean isTokenValid(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        // 检查token是否存在于redis中
        return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_PREFIX + token));
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StrUtil.isNotBlank(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
