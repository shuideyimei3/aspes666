package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.dto.PaymentRequest;
import cn.aspes.agri.trade.entity.PaymentRecord;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 支付记录服务
 */
public interface PaymentRecordService extends IService<PaymentRecord> {
    
    /**
     * 提交支付
     */
    Long submitPayment(PaymentRequest request);
    
    /**
     * 确认支付成功
     */
    void confirmPayment(Long paymentId, String paymentNo);
    
    /**
     * 查询订单的支付记录
     */
    List<PaymentRecord> listByOrder(Long orderId);
    
    /**
     * 分页查询支付记录
     */
    Page<PaymentRecord> pagePayments(Integer current, Integer size, Long orderId, String status);
    
    /**
     * 标记支付失败
     */
    void markPaymentFailed(Long paymentId, String reason);
    
    /**
     * 查询我的支付记录
     */
    Page<PaymentRecord> listMyPayments(Long userId, String role, Integer current, Integer size);
    
    /**
     * 分页查询所有支付记录（管理员使用）
     */
    Page<PaymentRecord> pageAllPayments(Integer current, Integer size, Long orderId, String status);
    
    /**
     * 根据采购方ID分页查询支付记录（管理员使用）
     */
    Page<PaymentRecord> pagePaymentsByPurchaserId(Long purchaserId, Integer current, Integer size, String status);
    
    /**
     * 删除支付记录（管理员操作）
     */
    void deletePayment(Long paymentId);
}