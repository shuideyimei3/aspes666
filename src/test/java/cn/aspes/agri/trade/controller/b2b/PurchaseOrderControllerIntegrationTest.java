package cn.aspes.agri.trade.controller.b2b;

import cn.aspes.agri.trade.dto.LoginRequest;
import cn.aspes.agri.trade.dto.LoginResponse;
import cn.aspes.agri.trade.dto.OrderDeliveryRequest;
import cn.aspes.agri.trade.entity.PurchaseOrder;
import cn.aspes.agri.trade.security.CustomUserDetails;
import cn.aspes.agri.trade.service.FarmerInfoService;
import cn.aspes.agri.trade.service.PurchaseOrderService;
import cn.aspes.agri.trade.service.PurchaserInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PurchaseOrderController.class)
@DisplayName("B端 - 采购订单控制器集成测试")
public class PurchaseOrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PurchaseOrderService orderService;

    @MockBean
    private PurchaserInfoService purchaserInfoService;

    @MockBean
    private FarmerInfoService farmerInfoService;

    private PurchaseOrder mockOrder;

    @BeforeEach
    void setUp() {
        // 创建模拟订单对象
        mockOrder = new PurchaseOrder();
        mockOrder.setId(1L);
        mockOrder.setContractId(1L);
        mockOrder.setPurchaserId(1L);
        mockOrder.setFarmerId(2L);
        mockOrder.setStatus("PENDING");
        mockOrder.setQuantity(100.0);
        mockOrder.setPrice(50.0);
        mockOrder.setTotalAmount(5000.0);

        // 模拟服务层方法
        Page<PurchaseOrder> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.singletonList(mockOrder));
        mockPage.setTotal(1);

        when(orderService.pageOrders(anyInt(), anyInt(), anyString())).thenReturn(mockPage);
        when(orderService.listMyOrders(anyLong(), anyString(), anyInt(), anyInt())).thenReturn(mockPage);
        when(orderService.createOrderFromContract(anyLong())).thenReturn(mockOrder);
        when(orderService.getOrderDetail(anyLong(), anyLong(), anyString())).thenReturn(mockOrder);
        doNothing().when(orderService).deliverOrder(anyLong(), anyDouble(), anyString());
        doNothing().when(orderService).completeOrder(anyLong());

        // 模拟采购方和农户信息服务
        when(purchaserInfoService.getByUserId(anyLong())).thenReturn(new cn.aspes.agri.trade.entity.PurchaserInfo());
        when(farmerInfoService.getByUserId(anyLong())).thenReturn(new cn.aspes.agri.trade.entity.FarmerInfo());
    }

    @Test
    @DisplayName("分页查询订单 - 成功")
    @WithMockUser(roles = {"ADMIN"})
    void testPageOrdersSuccess() throws Exception {
        mockMvc.perform(get("/api/b2b/orders/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("分页查询订单 - 带状态过滤")
    @WithMockUser(roles = {"ADMIN"})
    void testPageOrdersWithStatusFilter() throws Exception {
        mockMvc.perform(get("/api/b2b/orders/page")
                        .param("current", "1")
                        .param("size", "10")
                        .param("status", "pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("基于合同创建订单 - 成功")
    @WithMockUser(roles = {"PURCHASER"})
    void testCreateOrderFromContractSuccess() throws Exception {
        mockMvc.perform(post("/api/b2b/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.contractId").value(1));
    }

    @Test
    @DisplayName("基于合同创建订单 - 失败 - 无权限")
    @WithMockUser(roles = {"FARMER"})
    void testCreateOrderFromContractFailureWithoutPermission() throws Exception {
        mockMvc.perform(post("/api/b2b/orders/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("查询我的订单 - 采购方")
    @WithMockUser(roles = {"PURCHASER"})
    void testListMyOrdersAsPurchaser() throws Exception {
        mockMvc.perform(get("/api/b2b/orders/my")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("查询我的订单 - 农户")
    @WithMockUser(roles = {"FARMER"})
    void testListMyOrdersAsFarmer() throws Exception {
        mockMvc.perform(get("/api/b2b/orders/my")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("查询订单详情 - 成功")
    void testGetOrderDetailSuccess() throws Exception {
        mockMvc.perform(get("/api/b2b/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("查询订单详情 - 失败 - 订单不存在")
    void testGetOrderDetailFailureWithNonexistentOrder() throws Exception {
        mockMvc.perform(get("/api/b2b/orders/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("农户交货 - 成功")
    @WithMockUser(roles = {"FARMER"})
    void testDeliverOrderSuccess() throws Exception {
        OrderDeliveryRequest request = new OrderDeliveryRequest();
        request.setActualQuantity(50);
        request.setInspectionResult("质量合格，新鲜度高");

        mockMvc.perform(post("/api/b2b/orders/1/deliver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("农户交货 - 失败 - 无权限")
    @WithMockUser(roles = {"PURCHASER"})
    void testDeliverOrderFailureWithoutPermission() throws Exception {
        OrderDeliveryRequest request = new OrderDeliveryRequest();
        request.setActualQuantity(50);
        request.setInspectionResult("质量合格");

        mockMvc.perform(post("/api/b2b/orders/1/deliver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("采购方确认订单 - 成功")
    @WithMockUser(roles = {"PURCHASER"})
    void testCompleteOrderSuccess() throws Exception {
        mockMvc.perform(post("/api/b2b/orders/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("采购方确认订单 - 失败 - 无权限")
    @WithMockUser(roles = {"FARMER"})
    void testCompleteOrderFailureWithoutPermission() throws Exception {
        mockMvc.perform(post("/api/b2b/orders/1/complete"))
                .andExpect(status().isForbidden());
    }
}