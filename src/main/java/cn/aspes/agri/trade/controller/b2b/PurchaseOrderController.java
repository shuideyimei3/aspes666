package cn.aspes.agri.trade.controller.b2b;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.dto.OrderDeliveryRequest;
import cn.aspes.agri.trade.entity.PurchaseOrder;
import cn.aspes.agri.trade.security.CustomUserDetails;
import cn.aspes.agri.trade.service.FarmerInfoService;
import cn.aspes.agri.trade.service.PurchaseOrderService;
import cn.aspes.agri.trade.service.PurchaserInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * B端 - 采购订单管理控制器
 */
@Tag(name = "B端 - 采购订单")
@RestController
@RequestMapping("/api/b2b/orders")
public class PurchaseOrderController {
    
    @Resource
    private PurchaseOrderService orderService;
    
    @Resource
    private PurchaserInfoService purchaserInfoService;
    
    @Resource
    private FarmerInfoService farmerInfoService;
    
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
    public Result<PurchaseOrder> createOrderFromContract(@PathVariable Long contractId,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        PurchaseOrder order = orderService.createOrderFromContract(contractId);
        return Result.success(order);
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
    
    @Operation(summary = "农户交货")
    @PostMapping("/{id}/deliver")
    @PreAuthorize("hasRole('FARMER')")
    public Result<Void> deliverOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OrderDeliveryRequest request) {
        // 验证当前用户是否有权限操作该订单
        Long farmerId = farmerInfoService.getByUserId(userDetails.getId()).getId();
        PurchaseOrder order = orderService.getOrderDetail(id);
        if (order == null || !order.getFarmerId().equals(farmerId)) {
            throw new RuntimeException("无权限操作此订单");
        }
        
        orderService.deliverOrder(id, request.getActualQuantity(), request.getInspectionResult());
        return Result.success();
    }
    
    @Operation(summary = "采购方确认订单")
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('PURCHASER')")
    public Result<Void> completeOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // 验证当前用户是否有权限操作该订单
        Long purchaserId = purchaserInfoService.getByUserId(userDetails.getId()).getId();
        PurchaseOrder order = orderService.getOrderDetail(id);
        if (order == null || !order.getPurchaserId().equals(purchaserId)) {
            throw new RuntimeException("无权限操作此订单");
        }
        
        orderService.completeOrder(id);
        return Result.success();
    }
}