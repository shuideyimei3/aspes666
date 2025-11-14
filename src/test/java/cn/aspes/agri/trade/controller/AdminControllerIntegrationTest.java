package cn.aspes.agri.trade.controller;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.entity.*;
import cn.aspes.agri.trade.service.*;
import cn.aspes.agri.trade.vo.StatisticsVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = cn.aspes.agri.trade.AgriTradePlatformApplication.class)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@Rollback
@DisplayName("Admin控制器集成测试")
public class AdminControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;
    
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FarmerInfoService farmerInfoService;
    
    @Autowired
    private PurchaserInfoService purchaserInfoService;
    
    @Autowired
    private UserCertificationService certificationService;
    
    @Autowired
    private CooperationReviewService reviewService;
    
    @Autowired
    private StatisticsService statisticsService;
    
    private User adminUser;
    private User normalUser;
    private User farmerUser;
    private User purchaserUser;
    private FarmerInfo farmerInfo;
    private PurchaserInfo purchaserInfo;
    private UserCertificationApply certificationApply;
    private CooperationReview cooperationReview;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        
        // 创建管理员用户
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword("password");
        adminUser.setRole("ADMIN");
        adminUser.setIsDelete(0);
        userService.save(adminUser);
        
        // 创建普通用户
        normalUser = new User();
        normalUser.setUsername("user");
        normalUser.setPassword("password");
        normalUser.setRole("USER");
        normalUser.setIsDelete(0);
        userService.save(normalUser);
        
        // 创建农户用户
        farmerUser = new User();
        farmerUser.setUsername("farmer");
        farmerUser.setPassword("password");
        farmerUser.setRole("FARMER");
        farmerUser.setIsDelete(0);
        userService.save(farmerUser);
        
        // 创建采购方用户
        purchaserUser = new User();
        purchaserUser.setUsername("purchaser");
        purchaserUser.setPassword("password");
        purchaserUser.setRole("PURCHASER");
        purchaserUser.setIsDelete(0);
        userService.save(purchaserUser);
        
        // 创建农户信息
        farmerInfo = new FarmerInfo();
        farmerInfo.setUserId(farmerUser.getId());
        farmerInfo.setName("测试农户");
        farmerInfo.setAddress("测试地址");
        farmerInfo.setPhone("13800138000");
        farmerInfo.setAuditStatus("PENDING");
        farmerInfo.setOriginAreaId(1);
        farmerInfoService.save(farmerInfo);
        
        // 创建采购方信息
        purchaserInfo = new PurchaserInfo();
        purchaserInfo.setUserId(purchaserUser.getId());
        purchaserInfo.setName("测试采购方");
        purchaserInfo.setAddress("测试地址");
        purchaserInfo.setPhone("13900139000");
        purchaserInfo.setAuditStatus("PENDING");
        purchaserInfoService.save(purchaserInfo);
        
        // 创建认证申请
        certificationApply = new UserCertificationApply();
        certificationApply.setUserId(normalUser.getId());
        certificationApply.setApplyType("FARMER");
        certificationApply.setStatus("PENDING");
        certificationService.save(certificationApply);
        
        // 创建合作评价
        cooperationReview = new CooperationReview();
        cooperationReview.setReviewerId(farmerUser.getId());
        cooperationReview.setTargetId(purchaserUser.getId());
        cooperationReview.setRating(5);
        cooperationReview.setComment("很好");
        reviewService.save(cooperationReview);
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员分页查询用户列表")
    void testPageUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users/page")
                .param("current", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray());
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员按角色查询用户列表")
    void testPageUsersByRole() throws Exception {
        mockMvc.perform(get("/api/admin/users/page")
                .param("current", "1")
                .param("size", "10")
                .param("role", "FARMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray());
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员禁用用户")
    void testToggleUserStatus() throws Exception {
        mockMvc.perform(put("/api/admin/users/{userId}/status", normalUser.getId())
                .param("isDelete", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员分页查询农户信息")
    void testPageFarmers() throws Exception {
        mockMvc.perform(get("/api/admin/users/farmers/page")
                .param("current", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray());
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员按审核状态查询农户信息")
    void testPageFarmersByAuditStatus() throws Exception {
        mockMvc.perform(get("/api/admin/users/farmers/page")
                .param("current", "1")
                .param("size", "10")
                .param("auditStatus", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray());
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员审核农户信息")
    void testAuditFarmerInfo() throws Exception {
        String requestBody = "{\"auditStatus\":\"APPROVED\",\"auditRemark\":\"审核通过\"}";
        
        mockMvc.perform(put("/api/admin/users/farmers/{id}/audit", farmerInfo.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员分页查询采购方信息")
    void testPagePurchasers() throws Exception {
        mockMvc.perform(get("/api/admin/users/purchasers/page")
                .param("current", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray());
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员按审核状态查询采购方信息")
    void testPagePurchasersByAuditStatus() throws Exception {
        mockMvc.perform(get("/api/admin/users/purchasers/page")
                .param("current", "1")
                .param("size", "10")
                .param("auditStatus", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray());
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员审核采购方信息")
    void testAuditPurchaserInfo() throws Exception {
        String requestBody = "{\"auditStatus\":\"APPROVED\",\"auditRemark\":\"审核通过\"}";
        
        mockMvc.perform(put("/api/admin/users/purchasers/{id}/audit", purchaserInfo.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员分页查询待审核认证申请")
    void testPagePendingApplications() throws Exception {
        mockMvc.perform(get("/api/admin/certifications/pending")
                .param("current", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray());
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员按申请类型查询待审核认证申请")
    void testPagePendingApplicationsByType() throws Exception {
        mockMvc.perform(get("/api/admin/certifications/pending")
                .param("current", "1")
                .param("size", "10")
                .param("applyType", "FARMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray());
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员分页查询所有认证申请")
    void testPageApplications() throws Exception {
        mockMvc.perform(get("/api/admin/certifications/page")
                .param("current", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray());
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员按状态查询认证申请")
    void testPageApplicationsByStatus() throws Exception {
        mockMvc.perform(get("/api/admin/certifications/page")
                .param("current", "1")
                .param("size", "10")
                .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray());
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员批准认证申请")
    void testApproveCertification() throws Exception {
        mockMvc.perform(put("/api/admin/certifications/{applyId}/approve", certificationApply.getId())
                .param("adminRemark", "审核通过"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员拒绝认证申请")
    void testRejectCertification() throws Exception {
        mockMvc.perform(put("/api/admin/certifications/{applyId}/reject", certificationApply.getId())
                .param("rejectReason", "资料不完整"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员分页查询所有评价")
    void testPageReviews() throws Exception {
        mockMvc.perform(get("/api/admin/reviews/page")
                .param("current", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray());
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员按目标用户查询评价")
    void testPageReviewsByTargetId() throws Exception {
        mockMvc.perform(get("/api/admin/reviews/page")
                .param("current", "1")
                .param("size", "10")
                .param("targetId", purchaserUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray());
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员删除评价")
    void testDeleteReview() throws Exception {
        mockMvc.perform(delete("/api/admin/reviews/{reviewId}", cooperationReview.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员获取平台数据统计")
    void testGetPlatformStats() throws Exception {
        mockMvc.perform(get("/api/admin/statistics/platform"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }
    
    @Test
    @WithMockUser(roles = {"USER"})
    @DisplayName("普通用户访问管理员接口应被拒绝")
    void testNormalUserAccessDenied() throws Exception {
        mockMvc.perform(get("/api/admin/users/page"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = {"FARMER"})
    @DisplayName("农户用户访问管理员接口应被拒绝")
    void testFarmerUserAccessDenied() throws Exception {
        mockMvc.perform(get("/api/admin/users/page"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = {"PURCHASER"})
    @DisplayName("采购方用户访问管理员接口应被拒绝")
    void testPurchaserUserAccessDenied() throws Exception {
        mockMvc.perform(get("/api/admin/users/page"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("未认证用户访问管理员接口应被拒绝")
    void testUnauthenticatedUserAccessDenied() throws Exception {
        mockMvc.perform(get("/api/admin/users/page"))
                .andExpect(status().isUnauthorized());
    }
}