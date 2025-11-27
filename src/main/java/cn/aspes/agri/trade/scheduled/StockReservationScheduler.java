package cn.aspes.agri.trade.scheduled;

import cn.aspes.agri.trade.entity.StockReservation;
import cn.aspes.agri.trade.entity.FarmerProduct;
import cn.aspes.agri.trade.enums.ReservationStatus;
import cn.aspes.agri.trade.service.FarmerProductService;
import cn.aspes.agri.trade.mapper.StockReservationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存预留过期释放定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockReservationScheduler {
    
    private final StockReservationMapper stockReservationMapper;
    private final FarmerProductService farmerProductService;
    
    /**
     * 每小时执行一次，释放已过期的库存预留
     * 预留24小时后自动过期，此任务负责标记为过期并释放
     */
    @Scheduled(cron = "0 0 * * * *")
    public void releaseExpiredReservations() {
        try {
            // 查询已过期且状态为"预留"的预留记录
            List<StockReservation> expiredReservations = stockReservationMapper.selectList(
                    new LambdaQueryWrapper<StockReservation>()
                            .eq(StockReservation::getStatus, ReservationStatus.PENDING)
                            .lt(StockReservation::getExpiredTime, LocalDateTime.now())
            );
            
            if (expiredReservations.isEmpty()) {
                log.debug("没有过期的库存预留需要释放");
                return;
            }
            
            // 批量更新为已过期状态，并回补库存
            for (StockReservation reservation : expiredReservations) {
                FarmerProduct product = farmerProductService.getById(reservation.getProductId());
                if (product != null) {
                    product.setStock(product.getStock() + reservation.getReservedQuantity());
                    farmerProductService.updateById(product);
                }

                reservation.setStatus(ReservationStatus.EXPIRED);
                reservation.setReleaseReason("自动过期释放");
                stockReservationMapper.updateById(reservation);
                
                log.info("库存预留已过期释放：预留ID={}, 订单ID={}, 产品ID={}",
                        reservation.getId(), reservation.getOrderId(), reservation.getProductId());
            }
            
            log.info("本次释放已过期库存预留 {} 条", expiredReservations.size());
        } catch (Exception e) {
            log.error("释放过期库存预留出错", e);
        }
    }
}
