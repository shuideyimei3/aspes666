package cn.aspes.agri.trade.controller.admin;

import cn.aspes.agri.trade.entity.PaymentRecord;
import cn.aspes.agri.trade.entity.PurchaserInfo;
import cn.aspes.agri.trade.service.PaymentRecordService;
import cn.aspes.agri.trade.service.PurchaserInfoService;
import cn.aspes.agri.trade.converter.EntityVOConverter;
import cn.aspes.agri.trade.vo.PaymentRecordVO;
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

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminPaymentController.class)
@DisplayName("后台管理 - 支付管理控制器集成测试")
public class AdminPaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentRecordService paymentRecordService;

    @MockBean
    private PurchaserInfoService purchaserInfoService;

    @MockBean
    private EntityVOConverter entityVOConverter;

    private Page<PaymentRecord> mockPaymentPage;
    private Page<PaymentRecordVO> mockPaymentVOPage;

    @BeforeEach
    void setUp() {
        // 创建模拟支付记录
        PaymentRecord mockPayment = new PaymentRecord();
        mockPayment.setId(1L);
        mockPayment.setOrderId(1L);
        mockPayment.setPurchaserId(1L);
        mockPayment.setAmount(1000.0);
        mockPayment.setStatus("success");
        mockPayment.setPaymentMethod("alipay");
        mockPayment.setPaymentTime(java.time.LocalDateTime.now());

        // 创建模拟支付记录VO
        PaymentRecordVO mockPaymentVO = new PaymentRecordVO();
        mockPaymentVO.setId(1L);
        mockPaymentVO.setOrderId(1L);
        mockPaymentVO.setPurchaserId(1L);
        mockPaymentVO.setAmount(1000.0);
        mockPaymentVO.setStatus("success");
        mockPaymentVO.setPaymentMethod("alipay");
        mockPaymentVO.setPaymentTime(java.time.LocalDateTime.now());

        // 创建模拟分页对象
        mockPaymentPage = new Page<>(1, 10);
        mockPaymentPage.setRecords(Collections.singletonList(mockPayment));
        mockPaymentPage.setTotal(1);

        mockPaymentVOPage = new Page<>(1, 10);
        mockPaymentVOPage.setRecords(Collections.singletonList(mockPaymentVO));
        mockPaymentVOPage.setTotal(1);

        // 模拟服务方法
        when(paymentRecordService.pageAllPayments(anyInt(), anyInt(), any(), anyString())).thenReturn(mockPaymentPage);
        when(paymentRecordService.pagePaymentsByPurchaserId(anyLong(), anyInt(), anyInt(), anyString())).thenReturn(mockPaymentPage);
        when(entityVOConverter.toPaymentRecordVOPage(any(Page.class))).thenReturn(mockPaymentVOPage);

        // 模拟采购方信息
        PurchaserInfo mockPurchaser = new PurchaserInfo();
        mockPurchaser.setId(1L);
        mockPurchaser.setCompanyName("测试采购公司");
        when(purchaserInfoService.getByName("测试采购公司")).thenReturn(mockPurchaser);
        when(purchaserInfoService.getByName("不存在的公司")).thenReturn(null);

        // 模拟删除操作
        doNothing().when(paymentRecordService).deletePayment(anyLong());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页查询所有支付记录 - 成功")
    void testPageAllPaymentsSuccess() throws Exception {
        mockMvc.perform(get("/api/admin/payments/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页查询所有支付记录 - 带过滤条件")
    void testPageAllPaymentsWithFiltersSuccess() throws Exception {
        mockMvc.perform(get("/api/admin/payments/page")
                        .param("current", "1")
                        .param("size", "10")
                        .param("orderId", "1")
                        .param("status", "success"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @WithMockUser(roles = {"FARMER"})
    @DisplayName("分页查询所有支付记录 - 失败 - 无权限")
    void testPageAllPaymentsFailureWithoutPermission() throws Exception {
        mockMvc.perform(get("/api/admin/payments/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("分页查询所有支付记录 - 失败 - 未认证")
    void testPageAllPaymentsFailureWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/payments/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据采购方ID分页查询支付记录 - 成功")
    void testPagePaymentsByPurchaserIdSuccess() throws Exception {
        mockMvc.perform(get("/api/admin/payments/by-purchaser-id")
                        .param("purchaserId", "1")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据采购方ID分页查询支付记录 - 带状态过滤")
    void testPagePaymentsByPurchaserIdWithStatusSuccess() throws Exception {
        mockMvc.perform(get("/api/admin/payments/by-purchaser-id")
                        .param("purchaserId", "1")
                        .param("current", "1")
                        .param("size", "10")
                        .param("status", "success"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据采购方公司名称分页查询支付记录 - 成功")
    void testPagePaymentsByPurchaserNameSuccess() throws Exception {
        mockMvc.perform(get("/api/admin/payments/by-purchaser-name")
                        .param("purchaserName", "测试采购公司")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据采购方公司名称分页查询支付记录 - 公司不存在")
    void testPagePaymentsByPurchaserNameWithNonexistentCompany() throws Exception {
        mockMvc.perform(get("/api/admin/payments/by-purchaser-name")
                        .param("purchaserName", "不存在的公司")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("删除支付记录 - 成功")
    void testDeletePaymentSuccess() throws Exception {
        mockMvc.perform(delete("/api/admin/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"PURCHASER"})
    @DisplayName("删除支付记录 - 失败 - 无权限")
    void testDeletePaymentFailureWithoutPermission() throws Exception {
        mockMvc.perform(delete("/api/admin/payments/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("删除支付记录 - 失败 - 未认证")
    void testDeletePaymentFailureWithoutAuthentication() throws Exception {
        mockMvc.perform(delete("/api/admin/payments/1"))
                .andExpect(status().isUnauthorized());
    }
}