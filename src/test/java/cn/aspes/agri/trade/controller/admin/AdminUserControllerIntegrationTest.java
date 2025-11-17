package cn.aspes.agri.trade.controller.admin;

import cn.aspes.agri.trade.dto.AuditRequest;
import cn.aspes.agri.trade.entity.FarmerInfo;
import cn.aspes.agri.trade.entity.PurchaserInfo;
import cn.aspes.agri.trade.entity.User;
import cn.aspes.agri.trade.service.FarmerInfoService;
import cn.aspes.agri.trade.service.PurchaserInfoService;
import cn.aspes.agri.trade.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@DisplayName("后台管理 - 用户管理控制器集成测试")
public class AdminUserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private FarmerInfoService farmerInfoService;

    @MockBean
    private PurchaserInfoService purchaserInfoService;

    @BeforeEach
    void setUp() {
        // 模拟用户分页查询
        Page<User> userPage = new Page<>(1, 10);
        userPage.setRecords(new ArrayList<>());
        userPage.setTotal(0);
        when(userService.pageUsers(anyInt(), anyInt(), anyString(), any())).thenReturn(userPage);

        // 模拟农户信息分页查询
        Page<FarmerInfo> farmerPage = new Page<>(1, 10);
        farmerPage.setRecords(new ArrayList<>());
        farmerPage.setTotal(0);
        when(farmerInfoService.pageFarmers(anyInt(), anyInt(), anyString())).thenReturn(farmerPage);

        // 模拟采购方信息分页查询
        Page<PurchaserInfo> purchaserPage = new Page<>(1, 10);
        purchaserPage.setRecords(new ArrayList<>());
        purchaserPage.setTotal(0);
        when(purchaserInfoService.pagePurchasers(anyInt(), anyInt(), anyString())).thenReturn(purchaserPage);

        // 模拟用户状态切换
        doNothing().when(userService).toggleUserStatus(anyLong(), anyInt());

        // 模拟农户审核
        doNothing().when(farmerInfoService).auditFarmerInfo(anyLong(), any(AuditRequest.class));

        // 模拟采购方审核
        doNothing().when(purchaserInfoService).auditPurchaserInfo(anyLong(), any(AuditRequest.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页查询用户列表 - 成功")
    void testPageUsersSuccess() throws Exception {
        mockMvc.perform(get("/api/admin/users/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页查询用户列表 - 带过滤条件")
    void testPageUsersWithFiltersSuccess() throws Exception {
        mockMvc.perform(get("/api/admin/users/page")
                        .param("current", "1")
                        .param("size", "10")
                        .param("role", "FARMER")
                        .param("isCertified", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @WithMockUser(roles = {"FARMER"})
    @DisplayName("分页查询用户列表 - 失败 - 无权限")
    void testPageUsersFailureWithoutPermission() throws Exception {
        mockMvc.perform(get("/api/admin/users/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("分页查询用户列表 - 失败 - 未认证")
    void testPageUsersFailureWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/users/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("禁用/启用用户 - 成功")
    void testToggleUserStatusSuccess() throws Exception {
        mockMvc.perform(put("/api/admin/users/1/status")
                        .param("isDelete", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"PURCHASER"})
    @DisplayName("禁用/启用用户 - 失败 - 无权限")
    void testToggleUserStatusFailureWithoutPermission() throws Exception {
        mockMvc.perform(put("/api/admin/users/1/status")
                        .param("isDelete", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页查询农户信息 - 成功")
    void testPageFarmersSuccess() throws Exception {
        mockMvc.perform(get("/api/admin/users/farmers/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页查询农户信息 - 带审核状态过滤")
    void testPageFarmersWithAuditStatusSuccess() throws Exception {
        mockMvc.perform(get("/api/admin/users/farmers/page")
                        .param("current", "1")
                        .param("size", "10")
                        .param("auditStatus", "pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("审核农户信息 - 成功")
    void testAuditFarmerInfoSuccess() throws Exception {
        AuditRequest request = new AuditRequest();
        request.setAuditStatus("approved");
        request.setRemark("审核通过");

        mockMvc.perform(put("/api/admin/users/farmers/1/audit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页查询采购方信息 - 成功")
    void testPagePurchasersSuccess() throws Exception {
        mockMvc.perform(get("/api/admin/users/purchasers/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("审核采购方信息 - 成功")
    void testAuditPurchaserInfoSuccess() throws Exception {
        AuditRequest request = new AuditRequest();
        request.setAuditStatus("approved");
        request.setRemark("审核通过");

        mockMvc.perform(put("/api/admin/users/purchasers/1/audit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}