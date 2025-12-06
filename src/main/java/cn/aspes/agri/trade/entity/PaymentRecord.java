package cn.aspes.agri.trade.entity;

import cn.aspes.agri.trade.enums.PaymentMethod;
import cn.aspes.agri.trade.enums.PaymentStatus;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体类
 */
@Data
@TableName("payment_record")
public class PaymentRecord {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long orderId;
    
    private String paymentNo;
    
    private String paymentStage;
    
    private BigDecimal amount;
    
    private PaymentMethod paymentMethod;
    
    private PaymentStatus status;
    
    private LocalDateTime paymentTime;
    
    private String voucherUrl;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
