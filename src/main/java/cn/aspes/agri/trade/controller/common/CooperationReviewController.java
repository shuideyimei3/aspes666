package cn.aspes.agri.trade.controller.common;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.entity.CooperationReview;
import cn.aspes.agri.trade.security.CustomUserDetails;
import cn.aspes.agri.trade.service.CooperationReviewService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 通用 - 合作评价控制器
 */
@Tag(name = "通用 - 合作评价")
@RestController
@RequestMapping("/api/common/reviews")
public class CooperationReviewController {
    
    @Resource
    private CooperationReviewService reviewService;
    
    @Operation(summary = "分页查询评价")
    @GetMapping("/page")
    public Result<Page<CooperationReview>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long targetId) {
        Page<CooperationReview> page = new Page<>(current, size);
        if (targetId != null) {
            page = reviewService.lambdaQuery()
                    .eq(CooperationReview::getTargetId, targetId)
                    .page(page);
        } else {
            page = reviewService.page(page);
        }
        return Result.success(page);
    }
    
    @Operation(summary = "提交评价")
    @PostMapping
    @PreAuthorize("hasAnyRole('FARMER', 'PURCHASER')")
    public Result<Void> submit(@Valid @RequestBody CooperationReview review,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        reviewService.submitReview(review, userDetails.getId());
        return Result.success();
    }
    
    @Operation(summary = "查询我的评价")
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('FARMER', 'PURCHASER')")
    public Result<Page<CooperationReview>> listMyReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(reviewService.listMyReviews(userDetails.getId(), current, size));
    }
    
    @Operation(summary = "查询收到的评价")
    @GetMapping("/received")
    public Result<Page<CooperationReview>> listReceivedReviews(
            @RequestParam(required = true) Long targetId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(reviewService.listReceivedReviews(targetId, current, size));
    }
    
    @Operation(summary = "修改评价")
    @PutMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('FARMER', 'PURCHASER')")
    public Result<Void> updateReview(
            @PathVariable Long reviewId,
            @RequestParam(required = true) Integer rating,
            @RequestParam(required = false) String comment,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        reviewService.updateReview(reviewId, rating, comment, userDetails.getId());
        return Result.success();
    }
    
    @Operation(summary = "删除评价")
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('FARMER', 'PURCHASER')")
    public Result<Void> deleteReview(@PathVariable Long reviewId,
                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        reviewService.deleteReview(reviewId, userDetails.getId());
        return Result.success();
    }
}