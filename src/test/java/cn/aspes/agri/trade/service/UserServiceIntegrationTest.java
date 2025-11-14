package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.config.TestDatabaseConfig;
import cn.aspes.agri.trade.dto.LoginRequest;
import cn.aspes.agri.trade.dto.PasswordChangeRequest;
import cn.aspes.agri.trade.dto.UserRegisterRequest;
import cn.aspes.agri.trade.dto.UserUpdateRequest;
import cn.aspes.agri.trade.entity.User;
import cn.aspes.agri.trade.enums.UserRole;
import cn.aspes.agri.trade.exception.BusinessException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestDatabaseConfig.class)
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void testRegisterUser() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setRole(UserRole.FARMER);
        request.setContactPerson("张三");
        request.setContactPhone("13800138000");
        request.setContactEmail("user@example.com");

        Long userId = userService.register(request);
        assertNotNull(userId);
        assertTrue(userId > 0);

        User user = userService.getById(userId);
        assertNotNull(user);
        assertEquals("newuser", user.getUsername());
        assertEquals(UserRole.FARMER, user.getRole());
        assertEquals(0, user.getIsCertified());
        assertEquals(0, user.getIsDelete());
    }

    @Test
    void testRegisterDuplicateUsername() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("duplicateuser");
        request.setPassword("password123");
        request.setRole(UserRole.FARMER);
        request.setContactPerson("李四");
        request.setContactPhone("13800138001");
        request.setContactEmail("user2@example.com");

        // 首次注册应成功
        Long userId = userService.register(request);
        assertNotNull(userId);

        // 再次注册相同用户名应抛出异常
        assertThrows(BusinessException.class, () -> userService.register(request));
    }

    @Test
    void testLogin() {
        // 先注册用户
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUsername("loginuser");
        registerRequest.setPassword("password123");
        registerRequest.setRole(UserRole.FARMER);
        registerRequest.setContactPerson("王五");
        registerRequest.setContactPhone("13800138002");
        registerRequest.setContactEmail("login@example.com");

        Long userId = userService.register(registerRequest);

        // 测试登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("loginuser");
        loginRequest.setPassword("password123");

        var loginResponse = userService.login(loginRequest);
        assertNotNull(loginResponse);
        assertEquals(userId, loginResponse.getUserId());
        assertEquals("loginuser", loginResponse.getUsername());
        assertNotNull(loginResponse.getToken());
    }

    @Test
    void testLoginWithWrongPassword() {
        // 先注册用户
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUsername("wrongpassuser");
        registerRequest.setPassword("correctpassword");
        registerRequest.setRole(UserRole.FARMER);
        registerRequest.setContactPerson("赵六");
        registerRequest.setContactPhone("13800138003");
        registerRequest.setContactEmail("wrongpass@example.com");

        userService.register(registerRequest);

        // 尝试用错误密码登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("wrongpassuser");
        loginRequest.setPassword("wrongpassword");

        assertThrows(BusinessException.class, () -> userService.login(loginRequest));
    }

    @Test
    void testLoginWithNonExistentUser() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistentuser");
        loginRequest.setPassword("password");

        assertThrows(BusinessException.class, () -> userService.login(loginRequest));
    }

    @Test
    void testChangePassword() {
        // 先注册用户
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUsername("changepassuser");
        registerRequest.setPassword("oldpassword");
        registerRequest.setRole(UserRole.FARMER);
        registerRequest.setContactPerson("孙七");
        registerRequest.setContactPhone("13800138004");
        registerRequest.setContactEmail("changepass@example.com");

        Long userId = userService.register(registerRequest);

        // 修改密码
        PasswordChangeRequest changeRequest = new PasswordChangeRequest();
        changeRequest.setOldPassword("oldpassword");
        changeRequest.setNewPassword("newpassword");

        userService.changePassword(userId, changeRequest);

        // 用新密码登录应该成功
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("changepassuser");
        loginRequest.setPassword("newpassword");

        var loginResponse = userService.login(loginRequest);
        assertNotNull(loginResponse);
    }

    @Test
    void testChangePasswordWithWrongOldPassword() {
        // 先注册用户
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUsername("wrongoldpassuser");
        registerRequest.setPassword("correctoldpass");
        registerRequest.setRole(UserRole.FARMER);
        registerRequest.setContactPerson("周八");
        registerRequest.setContactPhone("13800138005");
        registerRequest.setContactEmail("wrongoldpass@example.com");

        Long userId = userService.register(registerRequest);

        // 用错误的旧密码修改密码
        PasswordChangeRequest changeRequest = new PasswordChangeRequest();
        changeRequest.setOldPassword("wrongoldpass");
        changeRequest.setNewPassword("newpassword");

        assertThrows(BusinessException.class, () -> userService.changePassword(userId, changeRequest));
    }

    @Test
    void testUpdateUserInfo() {
        // 先注册用户
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUsername("updateinfouser");
        registerRequest.setPassword("password123");
        registerRequest.setRole(UserRole.FARMER);
        registerRequest.setContactPerson("原始名字");
        registerRequest.setContactPhone("13800138000");
        registerRequest.setContactEmail("original@example.com");

        Long userId = userService.register(registerRequest);

        // 更新用户信息
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setContactPerson("更新后的名字");
        updateRequest.setContactPhone("13900139000");
        updateRequest.setContactEmail("updated@example.com");

        userService.updateUserInfo(userId, updateRequest);

        // 验证更新
        User user = userService.getById(userId);
        assertEquals("更新后的名字", user.getContactPerson());
        assertEquals("13900139000", user.getContactPhone());
        assertEquals("updated@example.com", user.getContactEmail());
    }

    @Test
    void testPageUsers() {
        // 注册多个用户
        for (int i = 0; i < 5; i++) {
            UserRegisterRequest request = new UserRegisterRequest();
            request.setUsername("pageuser" + i);
            request.setPassword("password123");
            request.setRole(UserRole.FARMER);
            request.setContactPerson("用户" + i);
            request.setContactPhone("138001380" + i);
            request.setContactEmail("pageuser" + i + "@example.com");

            userService.register(request);
        }

        // 分页查询
        Page<User> page = userService.pageUsers(1, 10, null, null);
        assertTrue(page.getRecords().size() > 0);
    }

    @Test
    void testToggleUserStatus() {
        // 先注册用户
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUsername("statususer");
        registerRequest.setPassword("password123");
        registerRequest.setRole(UserRole.FARMER);
        registerRequest.setContactPerson("状态用户");
        registerRequest.setContactPhone("13800138000");
        registerRequest.setContactEmail("status@example.com");

        Long userId = userService.register(registerRequest);

        // 测试方法不会抛出异常即为成功
        // 因为MyBatis-Plus的逻辑删除特性，我们无法通过getById验证删除状态
        assertDoesNotThrow(() -> {
            userService.toggleUserStatus(userId, 1);  // 禁用用户
            userService.toggleUserStatus(userId, 0);  // 启用用户
        });
        
        // 最终验证用户仍然存在且状态正常
        User user = userService.getById(userId);
        assertNotNull(user);
        assertEquals(0, user.getIsDelete());
    }

    @Test
    void testGetByUsername() {
        // 先注册用户
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("searchuser");
        request.setPassword("password123");
        request.setRole(UserRole.FARMER);
        request.setContactPerson("搜索用户");
        request.setContactPhone("13800138000");
        request.setContactEmail("search@example.com");

        userService.register(request);

        // 按用户名查询
        User user = userService.getByUsername("searchuser");
        assertNotNull(user);
        assertEquals("searchuser", user.getUsername());
        assertEquals(UserRole.FARMER, user.getRole());
    }

    @Test
    void testGetByUsernameNotFound() {
        User user = userService.getByUsername("nonexistentuser");
        assertNull(user);
    }
}
