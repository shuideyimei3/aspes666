package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.dto.LoginRequest;
import cn.aspes.agri.trade.dto.LoginResponse;
import cn.aspes.agri.trade.dto.UserRegisterRequest;
import cn.aspes.agri.trade.dto.UserUpdateRequest;
import cn.aspes.agri.trade.dto.PasswordChangeRequest;
import cn.aspes.agri.trade.entity.User;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    
    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);
    
    /**
     * 用户注册
     */
    Long register(UserRegisterRequest request);
    
    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);
    
    /**
     * 修改密码
     */
    void changePassword(Long userId, PasswordChangeRequest request);
    
    /**
     * 修改用户信息
     */
    void updateUserInfo(Long userId, UserUpdateRequest request);
    
    /**
     * 分页查询用户列表（管理员）
     */
    Page<User> pageUsers(Integer current, Integer size, String role, Integer isCertified);
    
    /**
     * 禁用/启用用户（管理员）
     */
    void toggleUserStatus(Long userId, Integer isDelete);
}
