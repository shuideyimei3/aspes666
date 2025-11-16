package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.entity.StockReservation;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 库存预留服务接口
 */
public interface StockReservationService extends IService<StockReservation> {
    
    /**
     * 创建订单时预留库存
     */
    Long reserveStock(Long orderId, Long productId, Integer quantity);
    
    /**
     * 创建订单时预留库存
     */
    boolean reserveStock(Long productId, Integer quantity, Long orderId);
    
    /**
     * 订单取消时释放预留库存
     */
    void releaseReservation(Long orderId, String reason);
    
    /**
     * 释放库存
     */
    void releaseStock(Long productId, Long orderId);
    
    /**
     * 支付成功后确认预留（不再自动释放）
     */
    void confirmReservation(Long reservationId);
    
    /**
     * 获取订单的库存预留记录
     */
    StockReservation getByOrderId(Long orderId);
}