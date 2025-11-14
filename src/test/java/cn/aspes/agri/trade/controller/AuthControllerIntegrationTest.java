package cn.aspes.agri.trade.controller;

import cn.aspes.agri.trade.dto.LoginRequest;
import cn.aspes.agri.trade.dto.LoginResponse;
import cn.aspes.agri.trade.dto.UserRegisterRequest;
import cn.aspes.agri.trade.enums.UserRole;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * 认证控制器集成测试
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "";
    }

    @Test
    void testRegisterAndLogin() {
        // 测试用户注册
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");
        registerRequest.setRole(UserRole.FARMER);
        registerRequest.setContactPerson("张三");
        registerRequest.setContactPhone("13800138000");
        registerRequest.setContactEmail("test@example.com");

        Long userId = given()
                .contentType(ContentType.JSON)
                .body(registerRequest)
                .when()
                .post("/api/common/auth/register")
                .then()
                .statusCode(200)
                .body("code", equalTo(0))
                .body("data", notNullValue())
                .extract()
                .path("data");

        assert userId > 0;

        // 测试用户登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/api/common/auth/login")
                .then()
                .statusCode(200)
                .body("code", equalTo(0))
                .body("data.token", notNullValue())
                .body("data.userId", equalTo(userId.intValue()))
                .body("data.username", equalTo("testuser"));
    }

    @Test
    void testRegisterDuplicateUsername() {
        // 注册第一个用户
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUsername("duplicate");
        registerRequest.setPassword("password123");
        registerRequest.setRole(UserRole.FARMER);
        registerRequest.setContactPerson("张三");
        registerRequest.setContactPhone("13800138000");
        registerRequest.setContactEmail("test@example.com");

        given()
                .contentType(ContentType.JSON)
                .body(registerRequest)
                .when()
                .post("/api/common/auth/register")
                .then()
                .statusCode(200)
                .body("code", equalTo(0));

        // 尝试注册相同用户名
        given()
                .contentType(ContentType.JSON)
                .body(registerRequest)
                .when()
                .post("/api/common/auth/register")
                .then()
                .statusCode(200)
                .body("code", not(0));
    }

    @Test
    void testLoginWithWrongPassword() {
        // 注册用户
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUsername("testuser2");
        registerRequest.setPassword("correctPassword");
        registerRequest.setRole(UserRole.FARMER);
        registerRequest.setContactPerson("李四");
        registerRequest.setContactPhone("13800138001");
        registerRequest.setContactEmail("test2@example.com");

        given()
                .contentType(ContentType.JSON)
                .body(registerRequest)
                .when()
                .post("/api/common/auth/register")
                .then()
                .statusCode(200)
                .body("code", equalTo(0));

        // 尝试使用错误密码登录
        LoginRequest wrongPasswordRequest = new LoginRequest();
        wrongPasswordRequest.setUsername("testuser2");
        wrongPasswordRequest.setPassword("wrongPassword");

        given()
                .contentType(ContentType.JSON)
                .body(wrongPasswordRequest)
                .when()
                .post("/api/common/auth/login")
                .then()
                .statusCode(200)
                .body("code", not(0));
    }

    @Test
    void testLoginWithNonExistentUser() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistent");
        loginRequest.setPassword("password");

        given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/api/common/auth/login")
                .then()
                .statusCode(200)
                .body("code", not(0));
    }

    @Test
    void testRegisterWithMissingFields() {
        // 缺少必要字段的注册请求
        String invalidRequest = "{}";

        given()
                .contentType(ContentType.JSON)
                .body(invalidRequest)
                .when()
                .post("/api/common/auth/register")
                .then()
                .statusCode(400);
    }
}
