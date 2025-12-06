package cn.aspes.agri.trade.controller.admin;

import cn.aspes.agri.trade.BaseTest;
import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.entity.PurchaseOrder;
import cn.aspes.agri.trade.enums.OrderStatus;
import cn.aspes.agri.trade.service.PurchaseOrderService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DisplayName("管理员订单控制器测试")
class AdminOrderControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private ObjectMapper objectMapper;

    private PurchaseOrder testOrder;
    private Page<PurchaseOrder> testPage;

    @BeforeEach
    void setUp() {
        // 创建测试订单
        testOrder = new PurchaseOrder();
        testOrder.setId(1L);
        testOrder.setOrderNo("PO20231227001");
        testOrder.setFarmerId(1L);
        testOrder.setPurchaserId(2L);
        testOrder.setProductId(3L);
        testOrder.setQuantity(100);
        testOrder.setTotalAmount(new java.math.BigDecimal("1000.00"));
        testOrder.setStatus(OrderStatus.PENDING);
        
        // 创建测试分页结果
        testPage = new Page<>(1, 10);
        testPage.setRecords(Arrays.asList(testOrder));
        testPage.setTotal(1);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页查询所有订单 - 成功")
    void pageOrders_Success() throws Exception {
        // 模拟服务返回
        when(purchaseOrderService.pageOrders(anyInt(), anyInt(), anyString()))
                .thenReturn(testPage);

        // 执行请求并验证结果
        mockMvc.perform(get("/api/admin/orders/page")
                        .param("current", "1")
                        .param("size", "10")
                        .param("status", "PENDING")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.records[0].orderNo").value("PO20231227001"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("查询订单详情 - 成功")
    void getOrderDetail_Success() throws Exception {
        // 模拟服务返回
        when(purchaseOrderService.getById(1L)).thenReturn(testOrder);

        // 执行请求并验证结果
        mockMvc.perform(get("/api/admin/orders/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.orderNo").value("PO20231227001"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("取消订单 - 成功")
    void cancelOrder_Success() throws Exception {
        // 模拟服务返回
        when(purchaseOrderService.getById(1L)).thenReturn(testOrder);

        // 执行请求并验证结果
        mockMvc.perform(put("/api/admin/orders/1/cancel")
                        .param("reason", "测试取消原因")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("强制完成订单 - 成功")
    void forceCompleteOrder_Success() throws Exception {
        // 模拟服务返回
        when(purchaseOrderService.getById(1L)).thenReturn(testOrder);

        // 执行请求并验证结果
        mockMvc.perform(put("/api/admin/orders/1/force-complete")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据订单状态查询订单列表 - 成功")
    void getOrdersByStatus_Success() throws Exception {
        // 模拟服务返回
        when(purchaseOrderService.pageOrders(anyInt(), anyInt(), anyString()))
                .thenReturn(testPage);

        // 执行请求并验证结果
        mockMvc.perform(get("/api/admin/orders/status/PENDING")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].orderNo").value("PO20231227001"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("获取订单统计信息 - 成功")
    void getOrderStatistics_Success() throws Exception {
        // 模拟服务返回
        when(purchaseOrderService.count()).thenReturn(10L);
        when(purchaseOrderService.count(any())).thenReturn(2L);

        // 执行请求并验证结果
        mockMvc.perform(get("/api/admin/orders/statistics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCount").value(10))
                .andExpect(jsonPath("$.data.pendingCount").value(2));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据农户ID查询订单 - 成功")
    void getOrdersByFarmer_Success() throws Exception {
        // 模拟服务返回
        when(purchaseOrderService.page(any(Page.class), any()))
                .thenReturn(testPage);

        // 执行请求并验证结果
        mockMvc.perform(get("/api/admin/orders/farmer/1")
                        .param("current", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.records[0].orderNo").value("PO20231227001"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据采购方ID查询订单 - 成功")
    void getOrdersByPurchaser_Success() throws Exception {
        // 模拟服务返回
        when(purchaseOrderService.page(any(Page.class), any()))
                .thenReturn(testPage);

        // 执行请求并验证结果
        mockMvc.perform(get("/api/admin/orders/purchaser/2")
                        .param("current", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.records[0].orderNo").value("PO20231227001"));
    }

    @Test
    @DisplayName("未授权访问 - 失败")
    void unauthorizedAccess_Failure() throws Exception {
        // 不使用@WithMockUser注解，模拟未授权访问
        mockMvc.perform(get("/api/admin/orders/page")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}