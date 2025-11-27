package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.entity.StockReservation;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 库存预留服务接口
 */
public interface StockReservationService extends IService<StockReservation> {
    
    /**
     * 创建订单时预留库存（预留即扣减）
     */
    Long reserveStock(Long orderId, Long productId, Integer quantity);

    /**
     * 订单取消/支付失败等场景释放预留（会回补库存）
     */
    void releaseReservation(Long orderId, String reason);

    /**
     * 支付成功后确认预留（不再自动释放，仅更新状态）
     */
    void confirmReservation(Long reservationId);

    /**
     * 根据订单获取当前有效预留记录（仅 PENDING）
     */
    StockReservation getActiveReservationByOrderId(Long orderId);
}
