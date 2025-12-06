package cn.aspes.agri.trade.controller.admin;

import cn.aspes.agri.trade.BaseTest;
import cn.aspes.agri.trade.entity.CooperationReview;
import cn.aspes.agri.trade.enums.UserRole;
import cn.aspes.agri.trade.security.CustomUserDetails;
import cn.aspes.agri.trade.service.CooperationReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminCooperationControllerTest extends BaseTest {

    @MockBean
    private CooperationReviewService reviewService;

    private CooperationReview testReview;

    @BeforeEach
    void setUp() {
        // 创建测试评价
        testReview = new CooperationReview();
        testReview.setId(1L);
        testReview.setOrderId(1L);
        testReview.setReviewFrom("farmer");
        testReview.setReviewTo("purchaser");
        testReview.setTargetId(2L);
        testReview.setRating(5);
        testReview.setComment("非常好的合作体验");
    }

    @Test
    @DisplayName("删除评价 - 成功")
    void deleteReview_Success() throws Exception {
        // 创建管理员用户详情
        CustomUserDetails adminUser = new CustomUserDetails(1L, "admin", "password", UserRole.ADMIN, true);
        
        // 模拟服务返回，使用任意Long值匹配userDetails.getId()
        doNothing().when(reviewService).deleteReview(anyLong(), anyLong());

        // 执行请求并验证结果
        mockMvc.perform(delete("/api/admin/reviews/1")
                        .with(user(adminUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("未授权访问 - 失败")
    void unauthorizedAccess_Failure() throws Exception {
        // 不使用用户认证，模拟未授权访问
        mockMvc.perform(delete("/api/admin/reviews/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}