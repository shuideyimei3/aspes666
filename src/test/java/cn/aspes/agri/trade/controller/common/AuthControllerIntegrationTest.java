package cn.aspes.agri.trade.controller.common;

import cn.aspes.agri.trade.dto.LoginRequest;
import cn.aspes.agri.trade.dto.LoginResponse;
import cn.aspes.agri.trade.dto.UserRegisterRequest;
import cn.aspes.agri.trade.entity.User;
import cn.aspes.agri.trade.enums.Role;
import cn.aspes.agri.trade.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("通用 - 认证控制器集成测试")
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        // 模拟登录成功
        LoginResponse successLoginResponse = new LoginResponse();
        successLoginResponse.setToken("test-jwt-token");
        successLoginResponse.setUserId(1L);
        successLoginResponse.setRole(Role.FARMER);
        when(userService.login(any(LoginRequest.class))).thenReturn(successLoginResponse);

        // 模拟登录失败 - 错误的用户名
        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("用户名或密码错误"));

        // 模拟注册成功
        when(userService.register(any(UserRegisterRequest.class))).thenReturn(2L);

        // 模拟注册失败 - 用户名已存在
        when(userService.register(any(UserRegisterRequest.class)))
                .thenThrow(new RuntimeException("用户名已存在"));

        // 模拟注册失败 - 无效的角色
        when(userService.register(any(UserRegisterRequest.class)))
                .thenThrow(new RuntimeException("无效的角色"));
    }

    @Test
    @DisplayName("用户登录 - 成功")
    void testLoginSuccess() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("farmer1");
        loginRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/common/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.userId").exists())
                .andExpect(jsonPath("$.data.role").exists())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseContent, LoginResponse.class);
        assertNotNull(loginResponse.getData().getToken());
        assertEquals("FARMER", loginResponse.getData().getRole().name());
    }

    @Test
    @DisplayName("用户登录 - 失败 - 错误的用户名")
    void testLoginFailureWithWrongUsername() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistentuser");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/common/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("用户登录 - 失败 - 错误的密码")
    void testLoginFailureWithWrongPassword() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("farmer1");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/common/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("用户注册 - 成功")
    void testRegisterSuccess() throws Exception {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setRole("farmer");
        registerRequest.setContactPerson("新用户");
        registerRequest.setContactPhone("13900139000");
        registerRequest.setContactEmail("newuser@example.com");

        mockMvc.perform(post("/api/common/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("用户注册 - 失败 - 用户名已存在")
    void testRegisterFailureWithExistingUsername() throws Exception {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUsername("farmer1"); // 已存在的用户名
        registerRequest.setPassword("password123");
        registerRequest.setRole("farmer");
        registerRequest.setContactPerson("重复用户");
        registerRequest.setContactPhone("13900139001");
        registerRequest.setContactEmail("duplicate@example.com");

        mockMvc.perform(post("/api/common/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("用户注册 - 失败 - 无效的角色")
    void testRegisterFailureWithInvalidRole() throws Exception {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUsername("invalidroleuser");
        registerRequest.setPassword("password123");
        registerRequest.setRole("invalidrole"); // 无效的角色
        registerRequest.setContactPerson("无效角色用户");
        registerRequest.setContactPhone("13900139002");
        registerRequest.setContactEmail("invalidrole@example.com");

        mockMvc.perform(post("/api/common/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").exists());
    }
}