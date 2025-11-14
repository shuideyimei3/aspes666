package cn.aspes.agri.trade.controller.b2b;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.entity.PurchaseOrder;
import cn.aspes.agri.trade.security.CustomUserDetails;
import cn.aspes.agri.trade.service.PurchaseOrderService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * B端 - 采购订单管理控制器
 */
@Tag(name = "B端 - 采购订单")
@RestController
@RequestMapping("/api/b2b/orders")
@RequiredArgsConstructor
public class PurchaseOrderController {
    
    private final PurchaseOrderService orderService;
    
    @Operation(summary = "分页查询订单")
    @GetMapping("/page")
    public Result<Page<PurchaseOrder>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        return Result.success(orderService.pageOrders(current, size, status));
    }
    
    @Operation(summary = "基于合同创建订单")
    @PostMapping("/{contractId}")
    @PreAuthorize("hasRole('PURCHASER')")
    public Result<Void> createOrderFromContract(@PathVariable Long contractId,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        orderService.createOrderFromContract(contractId);
        return Result.success();
    }
    
    @Operation(summary = "查询我的订单")
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('FARMER', 'PURCHASER')")
    public Result<Page<PurchaseOrder>> listMyOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        String role = userDetails.getRole().name().toLowerCase();
        return Result.success(orderService.listMyOrders(userDetails.getId(), role, current, size));
    }
    
    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<PurchaseOrder> getDetail(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id));
    }
}
