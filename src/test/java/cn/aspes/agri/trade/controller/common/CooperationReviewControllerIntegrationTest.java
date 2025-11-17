package cn.aspes.agri.trade.controller.common;

import cn.aspes.agri.trade.entity.CooperationReview;
import cn.aspes.agri.trade.service.CooperationReviewService;
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

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CooperationReviewController.class)
@DisplayName("通用 - 合作评价控制器集成测试")
public class CooperationReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CooperationReviewService reviewService;

    @BeforeEach
    void setUp() {
        // 模拟分页查询评价
        Page<CooperationReview> reviewPage = new Page<>(1, 10);
        reviewPage.setRecords(new ArrayList<>());
        reviewPage.setTotal(0);
        when(reviewService.page(any(Page.class))).thenReturn(reviewPage);
        when(reviewService.lambdaQuery()).thenReturn(new CooperationReviewService().lambdaQuery());

        // 模拟提交评价
        doNothing().when(reviewService).submitReview(any(CooperationReview.class), anyLong());

        // 模拟查询我的评价
        when(reviewService.listMyReviews(anyLong(), anyInt(), anyInt())).thenReturn(reviewPage);

        // 模拟查询收到的评价
        when(reviewService.listReceivedReviews(anyLong(), anyInt(), anyInt())).thenReturn(reviewPage);

        // 模拟修改评价
        doNothing().when(reviewService).updateReview(anyLong(), anyInt(), anyString(), anyLong());

        // 模拟删除评价
        doNothing().when(reviewService).deleteReview(anyLong(), anyLong());
    }

    @Test
    @DisplayName("分页查询评价 - 成功")
    void testPageReviewsSuccess() throws Exception {
        mockMvc.perform(get("/api/common/reviews/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").exists())
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    @DisplayName("分页查询评价 - 成功 - 带目标ID过滤")
    void testPageReviewsWithTargetIdSuccess() throws Exception {
        mockMvc.perform(get("/api/common/reviews/page")
                        .param("current", "1")
                        .param("size", "10")
                        .param("targetId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").exists())
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    @DisplayName("提交评价 - 成功 - 农户")
    @WithMockUser(roles = {"FARMER"})
    void testSubmitReviewFarmerSuccess() throws Exception {
        CooperationReview review = new CooperationReview();
        review.setTargetId(2L);
        review.setOrderId(1L);
        review.setRating(5);
        review.setComment("非常好的采购方，合作愉快！");

        mockMvc.perform(post("/api/common/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(review)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("提交评价 - 成功 - 采购方")
    @WithMockUser(roles = {"PURCHASER"})
    void testSubmitReviewPurchaserSuccess() throws Exception {
        CooperationReview review = new CooperationReview();
        review.setTargetId(1L);
        review.setOrderId(1L);
        review.setRating(4);
        review.setComment("农户产品质量不错，但交货时间稍有延迟");

        mockMvc.perform(post("/api/common/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(review)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("提交评价 - 失败 - 无权限")
    void testSubmitReviewNoPermission() throws Exception {
        CooperationReview review = new CooperationReview();
        review.setTargetId(2L);
        review.setOrderId(1L);
        review.setRating(5);
        review.setComment("非常好的采购方，合作愉快！");

        mockMvc.perform(post("/api/common/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(review)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("查询我的评价 - 成功 - 农户")
    @WithMockUser(username = "farmer1", roles = {"FARMER"})
    void testListMyReviewsFarmerSuccess() throws Exception {
        mockMvc.perform(get("/api/common/reviews/my")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").exists())
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    @DisplayName("查询我的评价 - 成功 - 采购方")
    @WithMockUser(username = "purchaser1", roles = {"PURCHASER"})
    void testListMyReviewsPurchaserSuccess() throws Exception {
        mockMvc.perform(get("/api/common/reviews/my")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").exists())
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    @DisplayName("查询我的评价 - 失败 - 无权限")
    void testListMyReviewsNoPermission() throws Exception {
        mockMvc.perform(get("/api/common/reviews/my")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("查询收到的评价 - 成功")
    void testListReceivedReviewsSuccess() throws Exception {
        mockMvc.perform(get("/api/common/reviews/received")
                        .param("targetId", "1")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").exists())
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    @DisplayName("修改评价 - 成功")
    void testUpdateReviewSuccess() throws Exception {
        mockMvc.perform(put("/api/common/reviews/1")
                        .param("rating", "4")
                        .param("comment", "修改后的评价内容"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("删除评价 - 成功")
    void testDeleteReviewSuccess() throws Exception {
        mockMvc.perform(delete("/api/common/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}