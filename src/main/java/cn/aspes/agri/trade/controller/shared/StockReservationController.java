package cn.aspes.agri.trade.controller.shared;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.entity.StockReservation;
import cn.aspes.agri.trade.service.StockReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 共享 - 库存预留控制器
 */
@Tag(name = "共享 - 库存预留")
@RestController
@RequestMapping("/api/shared/stock-reservation")
public class StockReservationController {
    
    @Resource
    private StockReservationService stockReservationService;
    
    @Operation(summary = "获取订单的库存预留记录")
    @GetMapping("/order/{orderId}")
    public Result<StockReservation> getReservationByOrder(@PathVariable Long orderId) {
        StockReservation reservation = stockReservationService.getByOrderId(orderId);
        return Result.success(reservation);
    }
    
    @Operation(summary = "确认库存预留（支付成功后调用）")
    @PutMapping("/{reservationId}/confirm")
    @PreAuthorize("hasAnyRole('FARMER','PURCHASER')")
    public Result<Void> confirmReservation(@PathVariable Long reservationId) {
        stockReservationService.confirmReservation(reservationId);
        return Result.success();
    }
}
