package cn.aspes.agri.trade.controller.admin;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.entity.PurchaseOrder;
import cn.aspes.agri.trade.enums.OrderStatus;
import cn.aspes.agri.trade.service.PurchaseOrderService;
import cn.aspes.agri.trade.vo.PurchaseOrderVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台管理 - 订单管理控制器
 * 
 * 提供管理员对平台订单的管理功能，包括订单查询、状态管理等
 */
@Tag(name = "后台管理 - 订单管理")
@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {
    
    @Resource
    private PurchaseOrderService purchaseOrderService;
    
    /**
     * 分页查询所有订单
     * 
     * @param current 当前页码
     * @param size 每页大小
     * @param status 订单状态
     * @return 订单列表分页结果
     */
    @Operation(summary = "分页查询所有订单")
    @GetMapping("/page")
    public Result<Page<PurchaseOrder>> pageOrders(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        return Result.success(purchaseOrderService.pageOrders(current, size, status));
    }
    
    /**
     * 根据订单ID查询订单详情
     * 
     * @param orderId 订单ID
     * @return 订单详情
     */
    @Operation(summary = "查询订单详情")
    @GetMapping("/{orderId}")
    public Result<PurchaseOrder> getOrderDetail(@PathVariable Long orderId) {
        // 管理员查看订单详情，不需要验证权限
        PurchaseOrder order = purchaseOrderService.getById(orderId);
        return Result.success(order);
    }
    
    /**
     * 取消订单
     * 
     * @param orderId 订单ID
     * @param reason 取消原因
     * @return 操作结果
     */
    @Operation(summary = "取消订单")
    @PutMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long orderId, @RequestParam(required = false) String reason) {
        purchaseOrderService.cancelOrder(orderId, reason != null ? reason : "管理员取消");
        return Result.success();
    }
    
    /**
     * 强制完成订单
     * 
     * @param orderId 订单ID
     * @return 操作结果
     */
    @Operation(summary = "强制完成订单")
    @PutMapping("/{orderId}/force-complete")
    public Result<Void> forceCompleteOrder(@PathVariable Long orderId) {
        // 管理员强制完成订单，使用管理员专用的方法
        purchaseOrderService.completeOrderByAdmin(orderId);
        return Result.success();
    }
    
    /**
     * 根据订单状态查询订单列表
     * 
     * @param status 订单状态
     * @return 订单列表
     */
    @Operation(summary = "根据订单状态查询订单列表")
    @GetMapping("/status/{status}")
    public Result<List<PurchaseOrder>> getOrdersByStatus(@PathVariable String status) {
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        Page<PurchaseOrder> page = purchaseOrderService.pageOrders(1, 1000, status);
        return Result.success(page.getRecords());
    }
    
    /**
     * 获取订单统计信息
     * 
     * @return 统计信息
     */
    @Operation(summary = "获取订单统计信息")
    @GetMapping("/statistics")
    public Result<Object> getOrderStatistics() {
        // 获取总订单数
        long totalCount = purchaseOrderService.count();
        
        // 获取各状态订单数
        long pendingCount = purchaseOrderService.count(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getStatus, OrderStatus.PENDING));
                
        long paidCount = purchaseOrderService.count(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getStatus, OrderStatus.PAID));
                
        long completedCount = purchaseOrderService.count(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getStatus, OrderStatus.COMPLETED));
                
        long cancelledCount = purchaseOrderService.count(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getStatus, OrderStatus.CANCELLED));
        
        // 构建统计结果
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", totalCount);
        statistics.put("pendingCount", pendingCount);
        statistics.put("paidCount", paidCount);
        statistics.put("completedCount", completedCount);
        statistics.put("cancelledCount", cancelledCount);
        
        return Result.success(statistics);
    }
    
    /**
     * 根据农户ID查询订单
     * 
     * @param farmerId 农户ID
     * @param current 当前页码
     * @param size 每页大小
     * @return 订单列表
     */
    @Operation(summary = "根据农户ID查询订单")
    @GetMapping("/farmer/{farmerId}")
    public Result<Page<PurchaseOrder>> getOrdersByFarmer(
            @PathVariable Long farmerId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<PurchaseOrder> page = new Page<>(current, size);
        Page<PurchaseOrder> result = purchaseOrderService.page(page, 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getFarmerId, farmerId)
                .orderByDesc(PurchaseOrder::getCreateTime));
        return Result.success(result);
    }
    
    /**
     * 根据采购方ID查询订单
     * 
     * @param purchaserId 采购方ID
     * @param current 当前页码
     * @param size 每页大小
     * @return 订单列表
     */
    @Operation(summary = "根据采购方ID查询订单")
    @GetMapping("/purchaser/{purchaserId}")
    public Result<Page<PurchaseOrder>> getOrdersByPurchaser(
            @PathVariable Long purchaserId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<PurchaseOrder> page = new Page<>(current, size);
        Page<PurchaseOrder> result = purchaseOrderService.page(page, 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getPurchaserId, purchaserId)
                .orderByDesc(PurchaseOrder::getCreateTime));
        return Result.success(result);
    }
}