package cn.aspes.agri.trade.vo;

import cn.aspes.agri.trade.enums.PaymentMethod;
import cn.aspes.agri.trade.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录视图对象
 */
@Data
public class PaymentRecordVO {
    
    private Long id;
    
    private Long orderId;
    
    private String orderNo;  // 关联订单编号
    
    private String paymentNo;
    
    private String paymentStage;
    
    private BigDecimal amount;
    
    private PaymentMethod paymentMethod;
    
    private PaymentStatus status;
    
    private LocalDateTime paymentTime;
    
    private String voucherUrl;
    
    private LocalDateTime createTime;
}
