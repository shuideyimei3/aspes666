package cn.aspes.agri.trade.service.impl;

import cn.aspes.agri.trade.dto.PaymentRequest;
import cn.aspes.agri.trade.entity.PaymentRecord;
import cn.aspes.agri.trade.entity.PurchaseOrder;
import cn.aspes.agri.trade.entity.PurchaseContract;
import cn.aspes.agri.trade.entity.FarmerInfo;
import cn.aspes.agri.trade.entity.PurchaserInfo;
import cn.aspes.agri.trade.enums.OrderStatus;
import cn.aspes.agri.trade.enums.PaymentStatus;
import cn.aspes.agri.trade.exception.BusinessException;
import cn.aspes.agri.trade.mapper.PaymentRecordMapper;
import cn.aspes.agri.trade.mapper.PurchaseOrderMapper;
import cn.aspes.agri.trade.service.PaymentRecordService;
import cn.aspes.agri.trade.service.PurchaseOrderService;
import cn.aspes.agri.trade.service.PurchaseContractService;
import cn.aspes.agri.trade.service.StockReservationService;
import cn.aspes.agri.trade.service.FarmerInfoService;
import cn.aspes.agri.trade.service.PurchaserInfoService;
import cn.aspes.agri.trade.service.FileUploadService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付记录服务实现
 */
@Slf4j
@Service
public class PaymentRecordServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord> implements PaymentRecordService {
    
    @Resource
    private PurchaseOrderService purchaseOrderService;
    
    @Resource
    private PurchaseOrderMapper purchaseOrderMapper;
    
    @Resource
    private PurchaseContractService contractService;
    
    @Resource
    private StockReservationService stockReservationService;
    
    @Resource
    private FarmerInfoService farmerInfoService;
    
    @Resource
    private PurchaserInfoService purchaserInfoService;
    
    @Resource
    private FileUploadService fileUploadService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitPayment(PaymentRequest request) {
        // 检查订单是否存在
        PurchaseOrder order = purchaseOrderService.getById(request.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 上传支付凭证文件（如果提供了）
        String voucherUrl = null;
        if (request.getVoucherFile() != null && !request.getVoucherFile().isEmpty()) {
            voucherUrl = fileUploadService.uploadPaymentVoucher(request.getVoucherFile());
        }
        
        // 创建支付记录
        PaymentRecord payment = new PaymentRecord();
        BeanUtils.copyProperties(request, payment);
        payment.setStatus(PaymentStatus.PENDING);
        // 设置支付凭证URL
        if (voucherUrl != null) {
            payment.setVoucherUrl(voucherUrl);
        }
        
        save(payment);
        return payment.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPayment(Long paymentId, String paymentNo) {
        PaymentRecord payment = getById(paymentId);
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }
        
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new BusinessException("该支付已确认，请勿重复操作");
        }
        
        // 确认支付
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaymentNo(paymentNo);
        payment.setPaymentTime(LocalDateTime.now());
        updateById(payment);
        
        // 跨模块业务流程协调：支付成功后自动更新订单状态
        updateOrderStatusAfterPayment(payment.getOrderId());
    }
    
    /**
     * 支付成功后自动更新订单为已支付状态
     */
    private void updateOrderStatusAfterPayment(Long orderId) {
        PurchaseOrder order = purchaseOrderMapper.selectById(orderId);
        if (order != null) {
            // ✅ 修复：检查是否已经是已支付状态，并提前退出，不进行重复更新
            if (order.getStatus() == OrderStatus.PAID || 
                order.getStatus() == OrderStatus.COMPLETED) {
                log.warn("订单已支付或已完成，不需要重复更新: orderId={}", orderId);
                return;
            }
            
            // ✅ 修复状态流转：支持PENDING和DELIVERED两种状态进行支付
            if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.DELIVERED) {
                order.setStatus(OrderStatus.PAID);
                purchaseOrderMapper.updateById(order);
                
                // 支付成功后，确认库存预留（在独立事务中处理，避免异常影响支付事务）
                confirmStockReservationAsync(orderId);
            }
        }
    }
    
    /**
     * 异步确认库存预留（独立事务）
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    private void confirmStockReservationAsync(Long orderId) {
        try {
            cn.aspes.agri.trade.entity.StockReservation reservation = stockReservationService.getByOrderId(orderId);
            if (reservation != null) {
                stockReservationService.confirmReservation(reservation.getId());
            }
        } catch (Exception e) {
            // 库存确认失败仅记录，不影响支付结果
            log.error("库存预留确认失败，订单={}", orderId, e);
        }
    }
    
    @Override
    public List<PaymentRecord> listByOrder(Long orderId) {
        return list(new LambdaQueryWrapper<PaymentRecord>()
                .eq(PaymentRecord::getOrderId, orderId)
                .orderByAsc(PaymentRecord::getCreateTime));
    }
    
    @Override
    public Page<PaymentRecord> pagePayments(Integer current, Integer size, Long orderId, String status) {
        Page<PaymentRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (orderId != null) {
            wrapper.eq(PaymentRecord::getOrderId, orderId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PaymentRecord::getStatus, PaymentStatus.valueOf(status.toUpperCase()));
        }
        
        wrapper.orderByDesc(PaymentRecord::getCreateTime);
        return page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markPaymentFailed(Long paymentId, String reason) {
        PaymentRecord payment = getById(paymentId);
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }
        
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("仅待支付状态的支付可标记为失败");
        }
        
        payment.setStatus(PaymentStatus.FAILED);
        updateById(payment);
        
        // ✅ 修复：支付失败时自动释放库存预留
        try {
            stockReservationService.releaseReservation(payment.getOrderId(), "支付失败");
        } catch (Exception e) {
            log.warn("库存预留释放失败，订单={}", payment.getOrderId(), e);
        }
    }
    
    @Override
    public Page<PaymentRecord> listMyPayments(Long userId, String role, Integer current, Integer size) {
        Page<PaymentRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        
        // 根据角色查询支付记录
        if ("farmer".equalsIgnoreCase(role)) {
            // 农户查询：查找农户该订单的所有支付记录
            FarmerInfo farmer = farmerInfoService.getByUserId(userId);
            if (farmer != null) {
                // ✅ 修复SQL注入：先查询合同ID，再查询订单ID，最后查支付
                List<PurchaseContract> contracts = contractService.list(
                        new LambdaQueryWrapper<PurchaseContract>()
                                .eq(PurchaseContract::getFarmerId, farmer.getId())
                                .select(PurchaseContract::getId)
                );
                if (!contracts.isEmpty()) {
                    List<Long> contractIds = contracts.stream()
                            .map(PurchaseContract::getId)
                            .collect(java.util.stream.Collectors.toList());
                    List<PurchaseOrder> orders = purchaseOrderService.list(
                            new LambdaQueryWrapper<PurchaseOrder>()
                                    .in(PurchaseOrder::getContractId, contractIds)
                                    .select(PurchaseOrder::getId)
                    );
                    if (!orders.isEmpty()) {
                        List<Long> orderIds = orders.stream()
                                .map(PurchaseOrder::getId)
                                .collect(java.util.stream.Collectors.toList());
                        wrapper.in(PaymentRecord::getOrderId, orderIds);
                    } else {
                        return page(page, new LambdaQueryWrapper<PaymentRecord>().eq(PaymentRecord::getId, -1L));
                    }
                } else {
                    return page(page, new LambdaQueryWrapper<PaymentRecord>().eq(PaymentRecord::getId, -1L));
                }
            } else {
                return page(page, new LambdaQueryWrapper<PaymentRecord>().eq(PaymentRecord::getId, -1L));
            }
        } else if ("purchaser".equalsIgnoreCase(role)) {
            // 采购方查询：该采购方该订单的所有支付记录
            PurchaserInfo purchaser = purchaserInfoService.getByUserId(userId);
            if (purchaser != null) {
                // ✅ 修复SQL注入：先查询合同ID，再查询订单ID，最后查支付
                List<PurchaseContract> contracts = contractService.list(
                        new LambdaQueryWrapper<PurchaseContract>()
                                .eq(PurchaseContract::getPurchaserId, purchaser.getId())
                                .select(PurchaseContract::getId)
                );
                if (!contracts.isEmpty()) {
                    List<Long> contractIds = contracts.stream()
                            .map(PurchaseContract::getId)
                            .collect(java.util.stream.Collectors.toList());
                    List<PurchaseOrder> orders = purchaseOrderService.list(
                            new LambdaQueryWrapper<PurchaseOrder>()
                                    .in(PurchaseOrder::getContractId, contractIds)
                                    .select(PurchaseOrder::getId)
                    );
                    if (!orders.isEmpty()) {
                        List<Long> orderIds = orders.stream()
                                .map(PurchaseOrder::getId)
                                .collect(java.util.stream.Collectors.toList());
                        wrapper.in(PaymentRecord::getOrderId, orderIds);
                    } else {
                        return page(page, new LambdaQueryWrapper<PaymentRecord>().eq(PaymentRecord::getId, -1L));
                    }
                } else {
                    return page(page, new LambdaQueryWrapper<PaymentRecord>().eq(PaymentRecord::getId, -1L));
                }
            } else {
                return page(page, new LambdaQueryWrapper<PaymentRecord>().eq(PaymentRecord::getId, -1L));
            }
        } else {
            // 其他角色返回空
            return page(page, new LambdaQueryWrapper<PaymentRecord>().eq(PaymentRecord::getId, -1L));
        }
        
        wrapper.orderByDesc(PaymentRecord::getCreateTime);
        return page(page, wrapper);
    }
}
