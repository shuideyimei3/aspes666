package cn.aspes.agri.trade.controller.b2b;

import cn.aspes.agri.trade.dto.PaymentRequest;
import cn.aspes.agri.trade.entity.PaymentRecord;
import cn.aspes.agri.trade.security.CustomUserDetails;
import cn.aspes.agri.trade.service.PaymentRecordService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
public class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentRecordService paymentRecordService;

    @BeforeEach
    void setUp() {
        // 模拟提交支付返回值
        when(paymentRecordService.submitPayment(any(PaymentRequest.class)))
                .thenReturn(1L);

        // 模拟分页查询支付记录
        Page<PaymentRecord> paymentPage = new Page<>(1, 10);
        paymentPage.setRecords(Collections.emptyList());
        paymentPage.setTotal(0);
        when(paymentRecordService.pagePayments(anyInt(), anyInt(), anyLong(), anyString()))
                .thenReturn(paymentPage);

        // 模拟查询我的支付记录
        when(paymentRecordService.listMyPayments(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(paymentPage);
    }

    @Test
    @DisplayName("提交支付 - 成功")
    @WithMockUser(roles = {"PURCHASER"})
    void testSubmitPaymentSuccess() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(1L);
        request.setPaymentMethod("BANK_TRANSFER");
        request.setAmount(1000.00);
        request.setRemark("测试支付");

        MockMultipartFile proofFile = new MockMultipartFile(
                "proofImage", 
                "payment_proof.jpg", 
                MediaType.IMAGE_JPEG_VALUE, 
                "payment proof image content".getBytes());

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", 
                "", 
                MediaType.APPLICATION_JSON_VALUE, 
                objectMapper.writeValueAsString(request).getBytes());

        mockMvc.perform(multipart("/api/b2b/payments")
                        .file(proofFile)
                        .file(requestPart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("分页查询支付记录 - 成功")
    void testPagePaymentsSuccess() throws Exception {
        mockMvc.perform(get("/api/b2b/payments/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("分页查询支付记录 - 带过滤条件")
    void testPagePaymentsWithFilters() throws Exception {
        mockMvc.perform(get("/api/b2b/payments/page")
                        .param("current", "1")
                        .param("size", "10")
                        .param("orderId", "1")
                        .param("status", "pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("查询我的支付记录 - 采购方")
    @WithMockUser(roles = {"PURCHASER"})
    void testListMyPaymentsAsPurchaser() throws Exception {
        mockMvc.perform(get("/api/b2b/payments/my")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("查询我的支付记录 - 农户")
    @WithMockUser(roles = {"FARMER"})
    void testListMyPaymentsAsFarmer() throws Exception {
        mockMvc.perform(get("/api/b2b/payments/my")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("查询我的支付记录 - 失败 - 未登录")
    void testListMyPaymentsFailureWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/b2b/payments/my")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized());
    }
}