package cn.aspes.agri.trade.controller.admin;

import cn.aspes.agri.trade.converter.EntityVOConverter;
import cn.aspes.agri.trade.entity.PaymentRecord;
import cn.aspes.agri.trade.entity.PurchaserInfo;
import cn.aspes.agri.trade.service.PaymentRecordService;
import cn.aspes.agri.trade.service.PurchaserInfoService;
import cn.aspes.agri.trade.vo.PaymentRecordVO;
import cn.aspes.agri.trade.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 后台管理 - 支付管理
 */
@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@Tag(name = "后台管理 - 支付管理")
public class AdminPaymentController {
    
    private final PaymentRecordService paymentRecordService;
    private final PurchaserInfoService purchaserInfoService;
    private final EntityVOConverter entityVOConverter;
    
    /**
     * 分页查询所有支付记录
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询所有支付记录")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<PaymentRecordVO>> pageAllPayments(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String status) {
        
        Page<PaymentRecord> paymentPage = paymentRecordService.pageAllPayments(current, size, orderId, status);

        Page<PaymentRecordVO> voPage = entityVOConverter.toPaymentRecordVOPage(paymentPage);

        return Result.success(voPage);
    }

    /**
     * 分页查询采购方支付记录（根据采购方ID）
     */
    @GetMapping("/by-purchaser-id")
    @Operation(summary = "根据采购方ID分页查询支付记录")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<PaymentRecordVO>> pagePaymentsByPurchaserId(
            @RequestParam Long purchaserId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {

        Page<PaymentRecord> paymentPage = paymentRecordService.pagePaymentsByPurchaserId(
                purchaserId, current, size, status);

        Page<PaymentRecordVO> voPage = entityVOConverter.toPaymentRecordVOPage(paymentPage);

        return Result.success(voPage);
    }

    /**
     * 分页查询采购方支付记录（根据采购方公司名称）
     */
    @GetMapping("/by-purchaser-name")
    @Operation(summary = "根据采购方公司名称分页查询支付记录")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<PaymentRecordVO>> pagePaymentsByPurchaserName(
            @RequestParam String purchaserName,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {

        // 根据公司名称查询采购方
        PurchaserInfo purchaserInfo = purchaserInfoService.getByName(purchaserName);
        if (purchaserInfo == null) {
            return Result.success(new Page<>());
        }

        // 查询该采购方的支付记录
        Page<PaymentRecord> paymentPage = paymentRecordService.pagePaymentsByPurchaserId(
                purchaserInfo.getId(), current, size, status);

        Page<PaymentRecordVO> voPage = entityVOConverter.toPaymentRecordVOPage(paymentPage);

        return Result.success(voPage);
    }
    
    /**
     * 删除支付记录
     */
    @DeleteMapping("/{paymentId}")
    @Operation(summary = "删除支付记录")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deletePayment(@PathVariable Long paymentId) {
        paymentRecordService.deletePayment(paymentId);
        return Result.success();
    }
}