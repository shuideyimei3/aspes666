package cn.aspes.agri.trade.controller.admin;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.entity.CooperationReview;
import cn.aspes.agri.trade.security.CustomUserDetails;
import cn.aspes.agri.trade.service.CooperationReviewService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理 - 合作评价管理控制器
 */
@Tag(name = "后台管理 - 评价管理")
@RestController
@RequestMapping("/api/admin/reviews")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCooperationController {
    
    @Resource
    private CooperationReviewService reviewService;

    
    @Operation(summary = "删除评价")
    @DeleteMapping("/{reviewId}")
    public Result<Void> deleteReview(@PathVariable Long reviewId,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        // 管理员删除评价，使用管理员的用户ID
        reviewService.deleteReview(reviewId, userDetails.getId());
        return Result.success();
    }
}