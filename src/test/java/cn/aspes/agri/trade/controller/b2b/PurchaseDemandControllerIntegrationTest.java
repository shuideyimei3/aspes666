package cn.aspes.agri.trade.controller.b2b;

import cn.aspes.agri.trade.dto.PurchaseDemandRequest;
import cn.aspes.agri.trade.service.EntityVOConverter;
import cn.aspes.agri.trade.service.PurchaseDemandService;
import cn.aspes.agri.trade.service.PurchaserInfoService;
import cn.aspes.agri.trade.vo.PurchaseDemandVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
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

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PurchaseDemandController.class)
public class PurchaseDemandControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PurchaseDemandService purchaseDemandService;

    @MockBean
    private PurchaserInfoService purchaserInfoService;

    @MockBean
    private EntityVOConverter entityVOConverter;

    @BeforeEach
    void setUp() {
        // Mock purchaser info service to return a valid purchaser ID
        when(purchaserInfoService.getByUserId(anyLong())).thenReturn(new cn.aspes.agri.trade.entity.PurchaserInfo());
    }

    @Test
    @DisplayName("发布采购需求 - 成功")
    @WithMockUser(roles = {"PURCHASER"})
    void testPublishDemandSuccess() throws Exception {
        // Mock service to return a demand ID
        when(purchaseDemandService.publishDemand(anyLong(), any(PurchaseDemandRequest.class))).thenReturn(1L);

        PurchaseDemandRequest request = new PurchaseDemandRequest();
        request.setCategoryId(2L);
        request.setProductName("测试需求产品");
        request.setSpecRequire("新鲜有机");
        request.setQuantity(100);
        request.setUnit("斤");
        request.setPriceRange("2.0-3.0");
        request.setDeliveryDate(LocalDate.now().plusDays(7));
        request.setDeliveryAddress("四川省成都市");
        request.setQualityRequire("新鲜有机蔬菜");

        mockMvc.perform(post("/api/b2b/demands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @DisplayName("发布采购需求 - 失败 - 无权限")
    @WithMockUser(roles = {"FARMER"})
    void testPublishDemandFailureWithoutPermission() throws Exception {
        PurchaseDemandRequest request = new PurchaseDemandRequest();
        request.setCategoryId(2L);
        request.setProductName("测试需求产品");
        request.setQuantity(50);
        request.setUnit("斤");

        mockMvc.perform(post("/api/b2b/demands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("发布采购需求 - 失败 - 未认证")
    void testPublishDemandFailureWithoutAuthentication() throws Exception {
        PurchaseDemandRequest request = new PurchaseDemandRequest();
        request.setCategoryId(2L);
        request.setProductName("测试需求产品");
        request.setQuantity(50);
        request.setUnit("斤");

        mockMvc.perform(post("/api/b2b/demands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("更新采购需求 - 成功")
    @WithMockUser(roles = {"PURCHASER"})
    void testUpdateDemandSuccess() throws Exception {
        // Mock service to do nothing
        doNothing().when(purchaseDemandService).updateDemand(anyLong(), anyLong(), any(PurchaseDemandRequest.class));

        PurchaseDemandRequest request = new PurchaseDemandRequest();
        request.setCategoryId(2L);
        request.setProductName("更新后的需求产品");
        request.setSpecRequire("新鲜有机");
        request.setQuantity(150);
        request.setUnit("斤");
        request.setPriceRange("2.5-3.5");
        request.setDeliveryDate(LocalDate.now().plusDays(10));
        request.setDeliveryAddress("四川省成都市");
        request.setQualityRequire("更新后的质量要求");

        mockMvc.perform(put("/api/b2b/demands/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("关闭需求 - 成功")
    @WithMockUser(roles = {"PURCHASER"})
    void testCloseDemandSuccess() throws Exception {
        // Mock service to do nothing
        doNothing().when(purchaseDemandService).closeDemand(anyLong(), anyLong());

        mockMvc.perform(put("/api/b2b/demands/1/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("开启需求 - 成功")
    @WithMockUser(roles = {"PURCHASER"})
    void testOpenDemandSuccess() throws Exception {
        // Mock service to do nothing
        doNothing().when(purchaseDemandService).openDemand(anyLong(), anyLong());

        mockMvc.perform(put("/api/b2b/demands/1/open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("查询需求列表 - 成功")
    void testListDemandsSuccess() throws Exception {
        // Mock service and converter
        IPage<cn.aspes.agri.trade.entity.PurchaseDemand> mockPage = mock(IPage.class);
        when(mockPage.getRecords()).thenReturn(Collections.emptyList());
        when(mockPage.getTotal()).thenReturn(0L);
        
        when(purchaseDemandService.listDemands(anyInt(), anyInt(), any(), any())).thenReturn(mockPage);
        
        IPage<PurchaseDemandVO> mockVoPage = mock(IPage.class);
        when(entityVOConverter.toPurchaseDemandVOPage(any(IPage.class))).thenReturn(mockVoPage);

        mockMvc.perform(get("/api/b2b/demands")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("查询需求列表 - 带过滤条件")
    void testListDemandsWithFilters() throws Exception {
        // Mock service and converter
        IPage<cn.aspes.agri.trade.entity.PurchaseDemand> mockPage = mock(IPage.class);
        when(mockPage.getRecords()).thenReturn(Collections.emptyList());
        when(mockPage.getTotal()).thenReturn(0L);
        
        when(purchaseDemandService.listDemands(anyInt(), anyInt(), eq(2L), eq("pending"))).thenReturn(mockPage);
        
        IPage<PurchaseDemandVO> mockVoPage = mock(IPage.class);
        when(entityVOConverter.toPurchaseDemandVOPage(any(IPage.class))).thenReturn(mockVoPage);

        mockMvc.perform(get("/api/b2b/demands")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("categoryId", "2")
                        .param("status", "pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("查询我的需求列表 - 成功")
    @WithMockUser(roles = {"PURCHASER"})
    void testListMyDemandsSuccess() throws Exception {
        // Mock service and converter
        IPage<cn.aspes.agri.trade.entity.PurchaseDemand> mockPage = mock(IPage.class);
        when(mockPage.getRecords()).thenReturn(Collections.emptyList());
        when(mockPage.getTotal()).thenReturn(0L);
        
        when(purchaseDemandService.listMyDemands(anyLong(), anyInt(), anyInt())).thenReturn(mockPage);
        
        IPage<PurchaseDemandVO> mockVoPage = mock(IPage.class);
        when(entityVOConverter.toPurchaseDemandVOPage(any(IPage.class))).thenReturn(mockVoPage);

        mockMvc.perform(get("/api/b2b/demands/my")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("查询需求详情 - 成功")
    void testGetDemandSuccess() throws Exception {
        // Mock service and converter
        cn.aspes.agri.trade.entity.PurchaseDemand mockDemand = new cn.aspes.agri.trade.entity.PurchaseDemand();
        mockDemand.setId(1L);
        mockDemand.setProductName("测试产品");
        
        when(purchaseDemandService.getById(1L)).thenReturn(mockDemand);
        
        PurchaseDemandVO mockVo = new PurchaseDemandVO();
        mockVo.setId(1L);
        mockVo.setProductName("测试产品");
        
        when(entityVOConverter.toPurchaseDemandVO(any(cn.aspes.agri.trade.entity.PurchaseDemand.class))).thenReturn(mockVo);

        mockMvc.perform(get("/api/b2b/demands/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.productName").value("测试产品"));
    }

    @Test
    @DisplayName("查询需求详情 - 失败 - 需求不存在")
    void testGetDemandFailureWithNonexistentDemand() throws Exception {
        // Mock service to return null
        when(purchaseDemandService.getById(99999L)).thenReturn(null);

        mockMvc.perform(get("/api/b2b/demands/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}