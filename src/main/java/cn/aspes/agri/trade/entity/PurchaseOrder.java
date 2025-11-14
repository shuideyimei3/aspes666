package cn.aspes.agri.trade.entity;

import cn.aspes.agri.trade.enums.OrderStatus;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 采购订单实体类
 */
@Data
@TableName(value = "purchase_order", autoResultMap = true)
public class PurchaseOrder {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private String orderNo;
    
    private Long contractId;
    
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> productInfo;
    
    private Integer actualQuantity;
    
    private BigDecimal actualAmount;
    
    private OrderStatus status;
    
    private LocalDateTime deliveryTime;
    
    private String inspectionResult;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
