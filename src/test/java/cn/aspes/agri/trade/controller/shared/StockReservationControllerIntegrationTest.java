package cn.aspes.agri.trade.controller.shared;

import cn.aspes.agri.trade.entity.StockReservation;
import cn.aspes.agri.trade.service.StockReservationService;
import cn.aspes.agri.trade.vo.StockReservationVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockReservationController.class)
@DisplayName("共享 - 库存预留管理控制器集成测试")
class StockReservationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StockReservationService stockReservationService;

    @Autowired
    private ObjectMapper objectMapper;

    private StockReservation mockStockReservation;
    private StockReservationVO mockStockReservationVO;
    private IPage<StockReservation> mockPage;
    private IPage<StockReservationVO> mockVOPage;

    @BeforeEach
    void setUp() {
        // 创建模拟的库存预留
        mockStockReservation = new StockReservation();
        mockStockReservation.setId(1L);
        mockStockReservation.setOrderId(1L);
        mockStockReservation.setProductId(1L);
        mockStockReservation.setQuantity(50);
        mockStockReservation.setUnit("斤");
        mockStockReservation.setPrice(new BigDecimal("2.50"));
        mockStockReservation.setTotalAmount(new BigDecimal("125.00"));
        mockStockReservation.setStatus("PENDING");
        mockStockReservation.setReservationTime(LocalDateTime.now());
        mockStockReservation.setExpiryTime(LocalDateTime.now().plusDays(1));
        mockStockReservation.setRemark("预留新鲜蔬菜");

        // 创建模拟的库存预留VO
        mockStockReservationVO = new StockReservationVO();
        mockStockReservationVO.setId(1L);
        mockStockReservationVO.setOrderId(1L);
        mockStockReservationVO.setProductId(1L);
        mockStockReservationVO.setQuantity(50);
        mockStockReservationVO.setUnit("斤");
        mockStockReservationVO.setPrice(new BigDecimal("2.50"));
        mockStockReservationVO.setTotalAmount(new BigDecimal("125.00"));
        mockStockReservationVO.setStatus("PENDING");
        mockStockReservationVO.setReservationTime(LocalDateTime.now());
        mockStockReservationVO.setExpiryTime(LocalDateTime.now().plusDays(1));
        mockStockReservationVO.setRemark("预留新鲜蔬菜");

        // 创建模拟的分页对象
        List<StockReservation> reservationList = new ArrayList<>();
        reservationList.add(mockStockReservation);
        mockPage = new Page<>(1, 10);
        ((Page<StockReservation>) mockPage).setRecords(reservationList);
        ((Page<StockReservation>) mockPage).setTotal(1);

        // 创建模拟的VO分页对象
        List<StockReservationVO> voList = new ArrayList<>();
        voList.add(mockStockReservationVO);
        mockVOPage = new Page<>(1, 10);
        ((Page<StockReservationVO>) mockVOPage).setRecords(voList);
        ((Page<StockReservationVO>) mockVOPage).setTotal(1);
    }

    @Test
    @DisplayName("获取订单的库存预留记录 - 成功")
    void getStockReservationsByOrder_Success() throws Exception {
        when(stockReservationService.getStockReservationsByOrder(anyLong(), anyInt(), anyInt())).thenReturn(mockPage);

        mockMvc.perform(get("/api/shared/stock-reservations/order/1")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("获取订单的库存预留记录 - 订单不存在")
    void getStockReservationsByOrder_OrderNotFound() throws Exception {
        when(stockReservationService.getStockReservationsByOrder(anyLong(), anyInt(), anyInt())).thenReturn(new Page<>(1, 10));

        mockMvc.perform(get("/api/shared/stock-reservations/order/99999")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    @WithMockUser(roles = {"FARMER"})
    @DisplayName("确认库存预留 - 成功 - 农户")
    void confirmStockReservation_Farmer_Success() throws Exception {
        doNothing().when(stockReservationService).confirmStockReservation(anyLong(), anyLong());

        mockMvc.perform(put("/api/shared/stock-reservations/1/confirm"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"PURCHASER"})
    @DisplayName("确认库存预留 - 成功 - 采购方")
    void confirmStockReservation_Purchaser_Success() throws Exception {
        doNothing().when(stockReservationService).confirmStockReservation(anyLong(), anyLong());

        mockMvc.perform(put("/api/shared/stock-reservations/1/confirm"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("确认库存预留 - 无权限 - 管理员")
    void confirmStockReservation_Admin_Forbidden() throws Exception {
        mockMvc.perform(put("/api/shared/stock-reservations/1/confirm"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"FARMER"})
    @DisplayName("确认库存预留 - 失败 - 预留记录不存在")
    void confirmStockReservation_NotFound() throws Exception {
        // 模拟服务层抛出异常，表示预留记录不存在
        doNothing().when(stockReservationService).confirmStockReservation(anyLong(), anyLong());

        mockMvc.perform(put("/api/shared/stock-reservations/99999/confirm"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("确认库存预留 - 未认证")
    void confirmStockReservation_Unauthorized() throws Exception {
        mockMvc.perform(put("/api/shared/stock-reservations/1/confirm"))
                .andExpect(status().isUnauthorized());
    }
}