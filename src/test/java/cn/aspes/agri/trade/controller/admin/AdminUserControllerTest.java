package cn.aspes.agri.trade.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import cn.aspes.agri.trade.BaseTest;
import cn.aspes.agri.trade.entity.User;
import cn.aspes.agri.trade.entity.FarmerInfo;
import cn.aspes.agri.trade.entity.PurchaserInfo;
import cn.aspes.agri.trade.enums.UserRole;
import cn.aspes.agri.trade.enums.AuditStatus;
import cn.aspes.agri.trade.dto.AuditRequest;
import cn.aspes.agri.trade.service.UserService;
import cn.aspes.agri.trade.service.FarmerInfoService;
import cn.aspes.agri.trade.service.PurchaserInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminUserControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private FarmerInfoService farmerInfoService;

    @MockBean
    private PurchaserInfoService purchaserInfoService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private FarmerInfo testFarmerInfo;
    private PurchaserInfo testPurchaserInfo;
    private Page<User> testUserPage;
    private Page<FarmerInfo> testFarmerPage;
    private Page<PurchaserInfo> testPurchaserPage;
    private AuditRequest testAuditRequest;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setRole(UserRole.FARMER);
        testUser.setIsCertified(1);
        
        // 创建测试农户信息
        testFarmerInfo = new FarmerInfo();
        testFarmerInfo.setId(1L);
        testFarmerInfo.setUserId(1L);
        testFarmerInfo.setFarmName("测试农场");
        testFarmerInfo.setAuditStatus(AuditStatus.PENDING);
        
        // 创建测试采购方信息
        testPurchaserInfo = new PurchaserInfo();
        testPurchaserInfo.setId(2L);
        testPurchaserInfo.setUserId(2L);
        testPurchaserInfo.setCompanyName("测试公司");
        testPurchaserInfo.setAuditStatus(AuditStatus.PENDING);
        
        // 创建测试分页结果
        testUserPage = new Page<>(1, 10);
        testUserPage.setRecords(Arrays.asList(testUser));
        testUserPage.setTotal(1);
        
        testFarmerPage = new Page<>(1, 10);
        testFarmerPage.setRecords(Arrays.asList(testFarmerInfo));
        testFarmerPage.setTotal(1);
        
        testPurchaserPage = new Page<>(1, 10);
        testPurchaserPage.setRecords(Arrays.asList(testPurchaserInfo));
        testPurchaserPage.setTotal(1);
        
        // 创建测试审核请求
        testAuditRequest = new AuditRequest();
        testAuditRequest.setAuditStatus(AuditStatus.APPROVED);
        testAuditRequest.setAuditRemark("审核通过");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页查询用户列表 - 成功")
    void pageUsers_Success() throws Exception {
        // 模拟服务返回
        when(userService.pageUsers(anyInt(), anyInt(), anyString(), anyInt()))
                .thenReturn(testUserPage);

        // 执行请求并验证结果
        mockMvc.perform(get("/api/admin/users/page")
                        .param("current", "1")
                        .param("size", "10")
                        .param("role", "FARMER")
                        .param("isCertified", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.records[0].username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("禁用/启用用户 - 成功")
    void toggleStatus_Success() throws Exception {
        // 模拟服务返回
        doNothing().when(userService).toggleUserStatus(anyLong(), anyInt());

        // 执行请求并验证结果
        mockMvc.perform(put("/api/admin/users/1/status")
                        .param("isDelete", "1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("强制用户下线 - 成功")
    void forceLogout_Success() throws Exception {
        // 模拟服务返回
        doNothing().when(userService).forceLogout(anyLong());

        // 执行请求并验证结果
        mockMvc.perform(post("/api/admin/users/1/force-logout")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页查询农户信息（审核） - 成功")
    void pageFarmers_Success() throws Exception {
        // 模拟服务返回
        when(farmerInfoService.pageFarmers(anyInt(), anyInt(), anyString()))
                .thenReturn(testFarmerPage);

        // 执行请求并验证结果
        mockMvc.perform(get("/api/admin/users/farmers/page")
                        .param("current", "1")
                        .param("size", "10")
                        .param("auditStatus", "PENDING")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.records[0].farmName").value("测试农场"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("审核农户信息 - 成功")
    void auditFarmerInfo_Success() throws Exception {
        // 模拟服务返回
        doNothing().when(farmerInfoService).auditFarmerInfo(anyLong(), any(AuditRequest.class));

        // 执行请求并验证结果
        mockMvc.perform(put("/api/admin/users/farmers/1/audit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAuditRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页查询采购方信息（审核） - 成功")
    void pagePurchasers_Success() throws Exception {
        // 模拟服务返回
        when(purchaserInfoService.pagePurchasers(anyInt(), anyInt(), anyString()))
                .thenReturn(testPurchaserPage);

        // 执行请求并验证结果
        mockMvc.perform(get("/api/admin/users/purchasers/page")
                        .param("current", "1")
                        .param("size", "10")
                        .param("auditStatus", "PENDING")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].id").value(2))
                .andExpect(jsonPath("$.data.records[0].companyName").value("测试公司"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("审核采购方信息 - 成功")
    void auditPurchaserInfo_Success() throws Exception {
        // 模拟服务返回
        doNothing().when(purchaserInfoService).auditPurchaserInfo(anyLong(), any(AuditRequest.class));

        // 执行请求并验证结果
        mockMvc.perform(put("/api/admin/users/purchasers/2/audit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAuditRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("未授权访问 - 失败")
    void unauthorizedAccess_Failure() throws Exception {
        // 不使用@WithMockUser注解，模拟未授权访问
        mockMvc.perform(get("/api/admin/users/page")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}