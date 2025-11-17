package cn.aspes.agri.trade.controller.admin;

import cn.aspes.agri.trade.service.StatisticsService;
import cn.aspes.agri.trade.vo.StatisticsVO;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminStatisticsController.class)
@DisplayName("后台管理 - 数据统计控制器集成测试")
public class AdminStatisticsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        // 模拟统计数据
        StatisticsVO.PlatformStats mockStats = new StatisticsVO.PlatformStats();
        mockStats.setTotalUsers(100);
        mockStats.setTotalFarmers(60);
        mockStats.setTotalPurchasers(40);
        mockStats.setTotalProducts(200);
        mockStats.setTotalOrders(150);
        mockStats.setTotalAmount(50000.0);

        when(statisticsService.getPlatformStats()).thenReturn(mockStats);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("获取平台数据统计 - 成功")
    void testGetPlatformStatsSuccess() throws Exception {
        mockMvc.perform(get("/api/admin/statistics/platform"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.totalUsers").value(100))
                .andExpect(jsonPath("$.data.totalFarmers").value(60))
                .andExpect(jsonPath("$.data.totalPurchasers").value(40))
                .andExpect(jsonPath("$.data.totalProducts").value(200))
                .andExpect(jsonPath("$.data.totalOrders").value(150))
                .andExpect(jsonPath("$.data.totalAmount").value(50000.0));
    }

    @Test
    @WithMockUser(roles = {"FARMER"})
    @DisplayName("获取平台数据统计 - 失败 - 无权限（农户）")
    void testGetPlatformStatsFailureAsFarmer() throws Exception {
        mockMvc.perform(get("/api/admin/statistics/platform"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"PURCHASER"})
    @DisplayName("获取平台数据统计 - 失败 - 无权限（采购方）")
    void testGetPlatformStatsFailureAsPurchaser() throws Exception {
        mockMvc.perform(get("/api/admin/statistics/platform"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("获取平台数据统计 - 失败 - 未认证")
    void testGetPlatformStatsFailureWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/statistics/platform"))
                .andExpect(status().isUnauthorized());
    }
}