package cn.aspes.agri.trade.service.impl;

import cn.aspes.agri.trade.dto.LoginRequest;
import cn.aspes.agri.trade.dto.LoginResponse;
import cn.aspes.agri.trade.dto.UserRegisterRequest;
import cn.aspes.agri.trade.dto.UserUpdateRequest;
import cn.aspes.agri.trade.dto.PasswordChangeRequest;
import cn.aspes.agri.trade.entity.User;
import cn.aspes.agri.trade.enums.UserRole;
import cn.aspes.agri.trade.exception.BusinessException;
import cn.aspes.agri.trade.mapper.UserMapper;
import cn.aspes.agri.trade.service.UserService;
import cn.aspes.agri.trade.util.JwtUtil;
import cn.aspes.agri.trade.util.SnowflakeIdGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SnowflakeIdGenerator idGenerator;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Value("${redis.login-fail-max-count:5}")
    private Integer loginFailMaxCount;
    
    @Value("${redis.login-fail-lock-time-minutes:30}")
    private Integer loginFailLockTimeMinutes;
    
    private static final String LOGIN_FAIL_PREFIX = "login:fail:";
    private static final String TOKEN_PREFIX = "token:";
    private static final String USER_TOKEN_PREFIX = "user:token:";
    
    @Override
    @Cacheable(value = "users", key = "'id:' + #id")
    public User getById(Serializable id) {
        return super.getById(id);
    }
    
    @Override
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();
        String failKey = LOGIN_FAIL_PREFIX + username;
        
        // 检查登录失败次数
        Integer failCount = (Integer) redisTemplate.opsForValue().get(failKey);
        if (failCount != null && failCount >= loginFailMaxCount) {
            throw new BusinessException("登录失败次数过多，请" + loginFailLockTimeMinutes + "分钟后再试");
        }
        
        // 查询用户（使用缓存）
        User user = getByUsername(username);
        
        if (user == null) {
            incrementLoginFailCount(failKey);
            throw new BusinessException("用户名或密码错误");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            incrementLoginFailCount(failKey);
            throw new BusinessException("用户名或密码错误");
        }
        
        // 登录成功，清除失败记录
        redisTemplate.delete(failKey);
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().getCode());
        
        // 将Token存入Redis，设置过期时间与JWT一致
        long expirationSeconds = jwtUtil.getExpiration() / 1000;
        redisTemplate.opsForValue().set(TOKEN_PREFIX + token, user.getId(), expirationSeconds, TimeUnit.SECONDS);
        
        // 记录用户的Token（支持强制下线）
        redisTemplate.opsForValue().set(USER_TOKEN_PREFIX + user.getId(), token, expirationSeconds, TimeUnit.SECONDS);
        
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRole().getCode());
    }
    
    /**
     * 增加登录失败次数
     */
    private void incrementLoginFailCount(String failKey) {
        Integer count = (Integer) redisTemplate.opsForValue().get(failKey);
        if (count == null) {
            redisTemplate.opsForValue().set(failKey, 1, loginFailLockTimeMinutes, TimeUnit.MINUTES);
        } else {
            redisTemplate.opsForValue().increment(failKey);
        }
    }
    
    @Override
    public void logout(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        // 从redis中删除token
        redisTemplate.delete(TOKEN_PREFIX + token);
        
        // 如果需要，也可以将token加入黑名单
        // redisTemplate.opsForValue().set("blacklist:" + token, true, expirationTime, TimeUnit.SECONDS);
    }
    
    @Override
    public boolean isTokenValid(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        // 检查token是否存在于redis中
        return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_PREFIX + token));
    }
    
    @Override
    public void forceLogout(Long userId) {
        // 获取用户的token
        String token = (String) redisTemplate.opsForValue().get(USER_TOKEN_PREFIX + userId);
        if (token != null) {
            // 删除token
            redisTemplate.delete(TOKEN_PREFIX + token);
            redisTemplate.delete(USER_TOKEN_PREFIX + userId);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public Long register(UserRegisterRequest request) {
        // 检查用户名是否已存在
        long count = count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        
        // 创建用户
        User user = new User();
        user.setId(idGenerator.nextId());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setContactPerson(request.getContactPerson());
        user.setContactPhone(request.getContactPhone());
        user.setContactEmail(request.getContactEmail());
        user.setIsCertified(0);
        user.setIsDelete(0);
        
        save(user);
        return user.getId();
    }
    
    @Override
    @Cacheable(value = "users", key = "'username:' + #username")
    public User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        
        // 设置新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        updateById(user);
        
        // 修改密码后强制用户下线
        forceLogout(userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public void updateUserInfo(Long userId, UserUpdateRequest request) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        user.setContactPerson(request.getContactPerson());
        user.setContactPhone(request.getContactPhone());
        user.setContactEmail(request.getContactEmail());
        updateById(user);
    }
    
    @Override
    public Page<User> pageUsers(Integer current, Integer size, String role, Integer isCertified) {
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (role != null && !role.isEmpty()) {
            wrapper.eq(User::getRole, UserRole.valueOf(role.toUpperCase()));
        }
        if (isCertified != null) {
            wrapper.eq(User::getIsCertified, isCertified);
        }
        wrapper.eq(User::getIsDelete, 0);
        wrapper.orderByDesc(User::getCreateTime);
        
        return page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public void toggleUserStatus(Long userId, Integer isDelete) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        user.setIsDelete(isDelete);
        updateById(user);
        
        // 如果是禁用用户，强制下线
        if (isDelete == 1) {
            forceLogout(userId);
        }
    }
    
    @Override
    @CacheEvict(value = "users", allEntries = true)
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }
}