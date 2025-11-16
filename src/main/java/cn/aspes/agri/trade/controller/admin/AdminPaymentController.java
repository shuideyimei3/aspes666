package cn.aspes.agri.trade.controller.admin;

import cn.aspes.agri.trade.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import cn.aspes.agri.trade.service.PaymentRecordService;

/**
 * 后台管理 - 支付管理控制器
 */
@Tag(name = "后台管理 - 支付管理")
@RestController
@RequestMapping("/api/admin/payments")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {
    
    private final PaymentRecordService paymentRecordService;
    
    public AdminPaymentController(PaymentRecordService paymentRecordService) {
        this.paymentRecordService = paymentRecordService;
    }
    
    /**
     * 确认支付
     */
    @Operation(summary = "确认支付")
    @PutMapping("/{paymentId}/confirm")
    public Result<Void> confirmPayment(@PathVariable Long paymentId,
                                        @RequestParam String paymentNo) {
        paymentRecordService.confirmPayment(paymentId, paymentNo);
        return Result.success();
    }
    
    /**
     * 标记支付失败
     */
    @Operation(summary = "标记支付失败")
    @PutMapping("/{paymentId}/fail")
    public Result<Void> markFailed(@PathVariable Long paymentId, @RequestParam String reason) {
        paymentRecordService.markPaymentFailed(paymentId, reason);
        return Result.success();
    }
}