package cn.aspes.agri.trade.service.impl;

import cn.aspes.agri.trade.entity.FarmerProduct;
import cn.aspes.agri.trade.entity.StockReservation;
import cn.aspes.agri.trade.enums.ReservationStatus;
import cn.aspes.agri.trade.exception.BusinessException;
import cn.aspes.agri.trade.mapper.StockReservationMapper;
import cn.aspes.agri.trade.service.FarmerProductService;
import cn.aspes.agri.trade.service.StockReservationService;
import cn.aspes.agri.trade.util.SnowflakeIdGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 库存预留服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockReservationServiceImpl extends ServiceImpl<StockReservationMapper, StockReservation> implements StockReservationService {
    
    private final FarmerProductService farmerProductService;
    private final SnowflakeIdGenerator idGenerator;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long reserveStock(Long orderId, Long productId, Integer quantity) {
        // 检查产品是否存在
        FarmerProduct product = farmerProductService.getById(productId);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        
        // 检查是否已预留过（订单+商品唯一）
        StockReservation existing = getOne(new LambdaQueryWrapper<StockReservation>()
                .eq(StockReservation::getOrderId, orderId)
                .eq(StockReservation::getProductId, productId));
        if (existing != null) {
            throw new BusinessException("该订单的库存已预留，请勿重复操作");
        }

        // 预留即扣减：在同一事务中减少可售库存，避免超卖
        if (product.getStock() < quantity) {
            throw new BusinessException("库存不足，无法预留");
        }
        product.setStock(product.getStock() - quantity);
        farmerProductService.updateById(product);

        // 创建预留记录
        StockReservation reservation = new StockReservation();
        reservation.setId(idGenerator.nextId());
        reservation.setOrderId(orderId);
        reservation.setProductId(productId);
        reservation.setReservedQuantity(quantity);
        reservation.setStatus(ReservationStatus.PENDING);
        // 预留24小时后自动过期（如未支付）
        reservation.setExpiredTime(LocalDateTime.now().plusHours(24));
        
        save(reservation);
        log.info("库存预留成功：订单={}, 产品={}, 数量={}", orderId, productId, quantity);
        
        return reservation.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseReservation(Long orderId, String reason) {
        StockReservation reservation = getActiveReservationByOrderId(orderId);
        if (reservation == null) {
            log.warn("释放预留时，预留记录不存在：订单={}", orderId);
            return;
        }
        
        if (reservation.getStatus() == ReservationStatus.RELEASED
                || reservation.getStatus() == ReservationStatus.EXPIRED
                || reservation.getStatus() == ReservationStatus.CONFIRMED) {
            log.warn("预留已非待预留状态，请勿重复操作：预留={}, 状态={}", reservation.getId(), reservation.getStatus());
            return;
        }
        
        // 回补库存（预留即扣减，对应释放必须恢复）
        FarmerProduct product = farmerProductService.getById(reservation.getProductId());
        if (product != null) {
            product.setStock(product.getStock() + reservation.getReservedQuantity());
            farmerProductService.updateById(product);
        }

        // 释放预留
        reservation.setStatus(ReservationStatus.RELEASED);
        reservation.setReleaseReason(reason);
        updateById(reservation);
        
        log.info("库存预留释放成功：预留={}, 原因={}", reservation.getId(), reason);
    }
    

    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReservation(Long reservationId) {
        StockReservation reservation = getById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预留记录不存在");
        }
        
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException("只有已预留的记录才能确认");
        }
        
        // 预留阶段已经扣减库存，这里只更新状态为已确认
        // 更新预留状态为已确认
        reservation.setStatus(ReservationStatus.CONFIRMED);
        updateById(reservation);
        
        log.info("库存预留已确认：产品={}, 数量={}", reservation.getProductId(), reservation.getReservedQuantity());
    }
    
    @Override
    public StockReservation getActiveReservationByOrderId(Long orderId) {
        return getOne(new LambdaQueryWrapper<StockReservation>()
                .eq(StockReservation::getOrderId, orderId)
                .eq(StockReservation::getStatus, ReservationStatus.PENDING));
    }
}