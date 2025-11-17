package cn.aspes.agri.trade.controller.b2b;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.converter.EntityVOConverter;
import cn.aspes.agri.trade.dto.PaymentRequest;
import cn.aspes.agri.trade.entity.PaymentRecord;
import cn.aspes.agri.trade.security.CustomUserDetails;
import cn.aspes.agri.trade.service.PaymentRecordService;
import cn.aspes.agri.trade.vo.PaymentRecordVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * B端 - 支付记录管理控制器
 */
@Tag(name = "B端 - 支付管理")
@RestController
@RequestMapping("/api/b2b/payments")
public class PaymentController {
    
    @Resource
    private PaymentRecordService paymentRecordService;
    
    @Resource
    private EntityVOConverter entityVOConverter;
    
    @Operation(summary = "提交支付")
    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('PURCHASER')")
    public Result<Long> submitPayment(
            @ModelAttribute PaymentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long paymentId = paymentRecordService.submitPayment(request, userDetails.getId());
        return Result.success(paymentId);
    }
    

    
    @Operation(summary = "分页查询支付记录")
    @GetMapping("/page")
    public Result<Page<PaymentRecord>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String status) {
        return Result.success(paymentRecordService.pagePayments(current, size, orderId, status));
    }
    

    
    @Operation(summary = "查询我的支付记录")
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('FARMER', 'PURCHASER')")
    public Result<Page<PaymentRecord>> listMyPayments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        String role = userDetails.getRole().name().toLowerCase();
        return Result.success(paymentRecordService.listMyPayments(userDetails.getId(), role, current, size));
    }
}