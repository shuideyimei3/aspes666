package cn.aspes.agri.trade.controller.b2c;

import cn.aspes.agri.trade.dto.LogisticsRequest;
import cn.aspes.agri.trade.dto.LogisticsTraceRequest;
import cn.aspes.agri.trade.entity.LogisticsRecord;
import cn.aspes.agri.trade.entity.LogisticsTrace;
import cn.aspes.agri.trade.service.LogisticsRecordService;
import cn.aspes.agri.trade.service.PurchaseOrderService;
import cn.aspes.agri.trade.converter.EntityVOConverter;
import cn.aspes.agri.trade.vo.LogisticsVO;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LogisticsController.class)
public class LogisticsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LogisticsRecordService logisticsRecordService;
    
    @MockBean
    private PurchaseOrderService purchaseOrderService;

    @MockBean
    private EntityVOConverter entityVOConverter;

    @BeforeEach
    void setUp() {
        // Mock service to return a logistics ID
        when(logisticsRecordService.createLogistics(any(LogisticsRequest.class))).thenReturn(1L);
        
        // Mock purchaseOrderService.deliverOrder to do nothing
        doNothing().when(purchaseOrderService).deliverOrder(anyLong(), anyInt(), anyString());
        
        // Mock service to return a logistics record
        LogisticsRecord mockLogistics = new LogisticsRecord();
        mockLogistics.setId(1L);
        mockLogistics.setOrderId(1L);
        mockLogistics.setLogisticsCompany("顺丰速运");
        mockLogistics.setTrackingNumber("SF1234567890");
        
        LogisticsVO mockLogisticsVO = new LogisticsVO();
        mockLogisticsVO.setId(1L);
        mockLogisticsVO.setOrderId(1L);
        mockLogisticsVO.setLogisticsCompany("顺丰速运");
        mockLogisticsVO.setTrackingNumber("SF1234567890");
        
        when(logisticsRecordService.getByOrderId(1L)).thenReturn(mockLogistics);
        when(logisticsRecordService.getByOrderId(99999L)).thenReturn(null);
        when(entityVOConverter.toLogisticsVO(mockLogistics)).thenReturn(mockLogisticsVO);
        
        // Mock service to return multiple logistics records for an order
        LogisticsRecord mockLogistics2 = new LogisticsRecord();
        mockLogistics2.setId(2L);
        mockLogistics2.setOrderId(1L);
        mockLogistics2.setLogisticsCompany("中通快递");
        mockLogistics2.setTrackingNumber("ZT9876543210");
        
        LogisticsVO mockLogisticsVO2 = new LogisticsVO();
        mockLogisticsVO2.setId(2L);
        mockLogisticsVO2.setOrderId(1L);
        mockLogisticsVO2.setLogisticsCompany("中通快递");
        mockLogisticsVO2.setTrackingNumber("ZT9876543210");
        
        when(logisticsRecordService.listByOrderId(1L)).thenReturn(java.util.Arrays.asList(mockLogistics, mockLogistics2));
        when(entityVOConverter.toLogisticsVO(mockLogistics2)).thenReturn(mockLogisticsVO2);
        
        // Mock service to return traces
        LogisticsTrace mockTrace = new LogisticsTrace();
        mockTrace.setId(1L);
        mockTrace.setLogisticsId(1L);
        mockTrace.setDescription("货物已从成都发出");
        mockTrace.setLocation("四川省成都市");
        
        when(logisticsRecordService.listTraces(1L)).thenReturn(Collections.singletonList(mockTrace));
        
        // Mock service to return page of traces
        Page<LogisticsTrace> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.singletonList(mockTrace));
        mockPage.setTotal(1);
        when(logisticsRecordService.pageTraces(1L, 1, 10)).thenReturn(mockPage);
    }

    @Test
    @DisplayName("创建物流记录 - 成功")
    @WithMockUser(roles = {"FARMER"})
    void testCreateLogisticsSuccess() throws Exception {
        LogisticsRequest request = new LogisticsRequest();
        request.setOrderId(1L);
        request.setLogisticsCompany("顺丰速运");
        request.setTrackingNumber("SF1234567890");
        request.setSenderName("张三");
        request.setSenderPhone("13800138000");
        request.setSenderAddress("四川省成都市");
        request.setReceiverName("李四");
        request.setReceiverPhone("13900139000");
        request.setReceiverAddress("北京市朝阳区");
        // 添加农户交货相关字段
        request.setActualQuantity(100);
        request.setInspectionResult("质量合格");

        mockMvc.perform(post("/api/b2c/logistics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @DisplayName("创建物流记录 - 失败 - 无权限")
    @WithMockUser(roles = {"PURCHASER"})
    void testCreateLogisticsFailureWithoutPermission() throws Exception {
        LogisticsRequest request = new LogisticsRequest();
        request.setOrderId(1L);
        request.setLogisticsCompany("顺丰速运");
        request.setTrackingNumber("SF1234567890");
        // 添加农户交货相关字段
        request.setActualQuantity(100);
        request.setInspectionResult("质量合格");

        mockMvc.perform(post("/api/b2c/logistics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("创建物流记录 - 失败 - 未认证")
    void testCreateLogisticsFailureWithoutAuthentication() throws Exception {
        LogisticsRequest request = new LogisticsRequest();
        request.setOrderId(1L);
        request.setLogisticsCompany("顺丰速运");
        request.setTrackingNumber("SF1234567890");
        // 添加农户交货相关字段
        request.setActualQuantity(100);
        request.setInspectionResult("质量合格");

        mockMvc.perform(post("/api/b2c/logistics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }



    @Test
    @DisplayName("添加物流轨迹 - 成功")
    @WithMockUser(roles = {"FARMER"})
    void testAddLogisticsTrackSuccess() throws Exception {
        // Mock service to do nothing
        doNothing().when(logisticsRecordService).addTrace(anyLong(), any(LogisticsTraceRequest.class));

        LogisticsTraceRequest request = new LogisticsTraceRequest();
        request.setDescription("货物已从成都发出");
        request.setLocation("四川省成都市");

        mockMvc.perform(post("/api/b2c/logistics/1/trace")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("添加物流轨迹 - 失败 - 未认证")
    void testAddLogisticsTrackFailureWithoutAuthentication() throws Exception {
        LogisticsTraceRequest request = new LogisticsTraceRequest();
        request.setDescription("货物已从成都发出");
        request.setLocation("四川省成都市");

        mockMvc.perform(post("/api/b2c/logistics/1/trace")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("确认签收 - 成功")
    @WithMockUser(roles = {"PURCHASER"})
    void testConfirmReceiptSuccess() throws Exception {
        // Mock service to do nothing
        doNothing().when(logisticsRecordService).confirmReceipt(anyLong());

        mockMvc.perform(put("/api/b2c/logistics/1/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("确认签收 - 失败 - 无权限")
    @WithMockUser(roles = {"FARMER"})
    void testConfirmReceiptFailureWithoutPermission() throws Exception {
        mockMvc.perform(put("/api/b2c/logistics/1/confirm"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("确认签收 - 失败 - 未认证")
    void testConfirmReceiptFailureWithoutAuthentication() throws Exception {
        mockMvc.perform(put("/api/b2c/logistics/1/confirm"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("查询订单物流信息 - 成功")
    void testGetOrderLogisticsSuccess() throws Exception {
        mockMvc.perform(get("/api/b2c/logistics/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.logisticsCompany").value("顺丰速运"));
    }

    @Test
    @DisplayName("查询订单物流信息 - 失败 - 订单不存在")
    void testGetOrderLogisticsFailureWithNonexistentOrder() throws Exception {
        mockMvc.perform(get("/api/b2c/logistics/order/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("查询物流轨迹列表 - 成功")
    void testGetLogisticsTracksSuccess() throws Exception {
        mockMvc.perform(get("/api/b2c/logistics/1/traces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].description").value("货物已从成都发出"));
    }

    @Test
    @DisplayName("分页查询物流轨迹 - 成功")
    void testPageLogisticsTracksSuccess() throws Exception {
        mockMvc.perform(get("/api/b2c/logistics/1/traces-page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
    }
}