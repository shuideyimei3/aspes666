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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SnowflakeIdGenerator idGenerator;
    
    @Override
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().getCode());
        
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRole().getCode());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
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
    public User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
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
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
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
    public void toggleUserStatus(Long userId, Integer isDelete) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        user.setIsDelete(isDelete);
        updateById(user);
    }
}
