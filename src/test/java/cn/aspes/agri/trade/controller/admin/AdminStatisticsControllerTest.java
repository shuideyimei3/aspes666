package cn.aspes.agri.trade.controller.admin;

import cn.aspes.agri.trade.BaseTest;
import cn.aspes.agri.trade.enums.UserRole;
import cn.aspes.agri.trade.security.CustomUserDetails;
import cn.aspes.agri.trade.service.StatisticsService;
import cn.aspes.agri.trade.vo.StatisticsVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("管理员数据统计控制器测试")
class AdminStatisticsControllerTest extends BaseTest {

    @MockBean
    private StatisticsService statisticsService;

    private StatisticsVO.PlatformStats testPlatformStats;
    private Map<String, Long> testFarmerActivity;

    @BeforeEach
    void setUp() {
        // 创建测试平台统计数据
        testPlatformStats = new StatisticsVO.PlatformStats();
        testPlatformStats.setTotalUsers(100L);
        testPlatformStats.setTotalFarmers(60L);
        testPlatformStats.setTotalPurchasers(40L);
        testPlatformStats.setTotalProducts(200L);
        testPlatformStats.setTotalOrders(150L);
        testPlatformStats.setTotalTransactionAmount(new java.math.BigDecimal("15000.0"));
        
        // 创建测试农户活跃度数据
        testFarmerActivity = new HashMap<>();
        testFarmerActivity.put("北京", 10L);
        testFarmerActivity.put("上海", 8L);
        testFarmerActivity.put("广州", 5L);
    }

    @Test
    @DisplayName("获取平台数据统计 - 成功")
    void getPlatformStats_Success() throws Exception {
        // 创建管理员用户详情
        CustomUserDetails adminUser = new CustomUserDetails(1L, "admin", "password", UserRole.ADMIN, true);
        
        // 模拟服务返回
        when(statisticsService.getPlatformStats()).thenReturn(testPlatformStats);

        // 执行请求并验证结果
        mockMvc.perform(get("/api/admin/statistics/platform")
                        .with(user(adminUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalUsers").value(100))
                .andExpect(jsonPath("$.data.totalFarmers").value(60))
                .andExpect(jsonPath("$.data.totalPurchasers").value(40))
                .andExpect(jsonPath("$.data.totalProducts").value(200))
                .andExpect(jsonPath("$.data.totalOrders").value(150))
                .andExpect(jsonPath("$.data.totalTransactionAmount").value(15000.0));
    }

    @Test
    @DisplayName("SSE实时推送市级农户活跃度 - 成功")
    void streamFarmerActivity_Success() throws Exception {
        // 创建管理员用户详情
        CustomUserDetails adminUser = new CustomUserDetails(1L, "admin", "password", UserRole.ADMIN, true);
        
        // 模拟服务返回
        when(statisticsService.getFarmerActivityByCity(anyInt())).thenReturn(testFarmerActivity);

        // 执行请求并验证结果
        mockMvc.perform(get("/api/admin/statistics/farmer-activity/stream")
                        .with(user(adminUser))
                        .param("windowMinutes", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/event-stream"));
    }

    @Test
    @DisplayName("未授权访问 - 失败")
    void unauthorizedAccess_Failure() throws Exception {
        // 不使用@WithMockUser注解，模拟未授权访问
        mockMvc.perform(get("/api/admin/statistics/platform")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}