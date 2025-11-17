package cn.aspes.agri.trade.controller.b2b;

import cn.aspes.agri.trade.dto.LoginRequest;
import cn.aspes.agri.trade.dto.LoginResponse;
import cn.aspes.agri.trade.dto.PurchaserInfoRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class PurchaserInfoControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String farmerToken;
    private String purchaserToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // 获取不同角色的token
        farmerToken = getToken("farmer1", "password123");
        purchaserToken = getToken("purchaser1", "password123");
        adminToken = getToken("admin", "password123");
    }

    private String getToken(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/common/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseContent, LoginResponse.class);
        return loginResponse.getData().getToken();
    }

    @Test
    @DisplayName("提交采购方信息 - 成功")
    void testSubmitPurchaserInfoSuccess() throws Exception {
        PurchaserInfoRequest request = new PurchaserInfoRequest();
        request.setCompanyName("测试采购公司");
        request.setBusinessLicense("91510100MA61X0XXXX");
        request.setLegalPerson("张三");
        request.setContactPerson("李四");
        request.setContactPhone("13800138000");
        request.setContactEmail("test@company.com");
        request.setBusinessScope("农产品采购");
        request.setRegisteredAddress("四川省成都市高新区");
        request.setRegisteredCapital("1000万元");
        request.setEstablishDate("2020-01-01");

        mockMvc.perform(post("/api/b2b/purchaser-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + purchaserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("提交采购方信息 - 失败 - 无权限")
    void testSubmitPurchaserInfoFailureWithoutPermission() throws Exception {
        PurchaserInfoRequest request = new PurchaserInfoRequest();
        request.setCompanyName("测试采购公司");
        request.setBusinessLicense("91510100MA61X0XXXX");
        request.setLegalPerson("张三");
        request.setContactPerson("李四");
        request.setContactPhone("13800138000");

        mockMvc.perform(post("/api/b2b/purchaser-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("获取采购方详情 - 成功")
    void testGetPurchaserInfoSuccess() throws Exception {
        mockMvc.perform(get("/api/b2b/purchaser-info/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("获取采购方详情 - 失败 - 采购方不存在")
    void testGetPurchaserInfoFailureWithNonexistentPurchaser() throws Exception {
        mockMvc.perform(get("/api/b2b/purchaser-info/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("获取当前用户的采购方信息 - 成功")
    void testGetCurrentPurchaserInfoSuccess() throws Exception {
        mockMvc.perform(get("/api/b2b/purchaser-info/current")
                        .header("Authorization", "Bearer " + purchaserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("获取当前用户的采购方信息 - 失败 - 无权限")
    void testGetCurrentPurchaserInfoFailureWithoutPermission() throws Exception {
        mockMvc.perform(get("/api/b2b/purchaser-info/current")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("获取当前用户的采购方信息 - 失败 - 未登录")
    void testGetCurrentPurchaserInfoFailureWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/b2b/purchaser-info/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("修改采购方信息 - 成功")
    void testUpdatePurchaserInfoSuccess() throws Exception {
        PurchaserInfoRequest request = new PurchaserInfoRequest();
        request.setCompanyName("更新后的采购公司");
        request.setContactPerson("王五");
        request.setContactPhone("13900139000");

        mockMvc.perform(put("/api/b2b/purchaser-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + purchaserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("修改采购方信息 - 失败 - 无权限")
    void testUpdatePurchaserInfoFailureWithoutPermission() throws Exception {
        PurchaserInfoRequest request = new PurchaserInfoRequest();
        request.setCompanyName("更新后的采购公司");
        request.setContactPerson("王五");
        request.setContactPhone("13900139000");

        mockMvc.perform(put("/api/b2b/purchaser-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }
}