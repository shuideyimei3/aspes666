package cn.aspes.agri.trade.controller.admin;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.entity.CooperationReview;
import cn.aspes.agri.trade.service.CooperationReviewService;
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

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminCooperationController.class)
@DisplayName("后台管理 - 合作评价管理控制器集成测试")
class AdminCooperationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CooperationReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    private Page<CooperationReview> mockPage;
    private CooperationReview mockReview;

    @BeforeEach
    void setUp() {
        mockReview = new CooperationReview();
        mockReview.setId(1L);
        mockReview.setTargetId(1L);
        mockReview.setReviewFrom("FARMER_2");
        mockReview.setRating(5);
        mockReview.setComment("非常好的合作体验");
        
        List<CooperationReview> reviewList = new ArrayList<>();
        reviewList.add(mockReview);
        
        mockPage = new Page<>(1, 10);
        mockPage.setRecords(reviewList);
        mockPage.setTotal(1);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页查询所有评价 - 成功")
    void pageReviews_Success() throws Exception {
        when(reviewService.page(any(Page.class))).thenReturn(mockPage);

        mockMvc.perform(get("/api/admin/reviews/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页查询所有评价 - 带目标ID过滤")
    void pageReviews_WithTargetId_Success() throws Exception {
        when(reviewService.lambdaQuery().eq(any(), any()).page(any(Page.class))).thenReturn(mockPage);

        mockMvc.perform(get("/api/admin/reviews/page")
                        .param("current", "1")
                        .param("size", "10")
                        .param("targetId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @WithMockUser(roles = {"FARMER"})
    @DisplayName("分页查询所有评价 - 无权限")
    void pageReviews_NoPermission() throws Exception {
        mockMvc.perform(get("/api/admin/reviews/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("删除评价 - 成功")
    void deleteReview_Success() throws Exception {
        doNothing().when(reviewService).deleteReview(1L, -1L);

        mockMvc.perform(delete("/api/admin/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"PURCHASER"})
    @DisplayName("删除评价 - 无权限")
    void deleteReview_NoPermission() throws Exception {
        mockMvc.perform(delete("/api/admin/reviews/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("分页查询所有评价 - 未认证")
    void pageReviews_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/reviews/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized());
    }
}