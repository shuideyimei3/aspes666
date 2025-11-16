package cn.aspes.agri.trade.entity;

import cn.aspes.agri.trade.enums.ContractStatus;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 采购合同实体类
 */
@Data
@TableName(value = "purchase_contract", autoResultMap = true)
public class PurchaseContract {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private String contractNo;
    
    private Long dockingId;
    
    private Long purchaserId;
    
    private Long farmerId;
    
    private Long productId;
    
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> productInfo;
    
    private Integer quantity;
    
    private BigDecimal totalAmount;
    
    private String paymentTerms;
    
    private LocalDate deliveryTime;
    
    private String deliveryAddress;
    
    private String qualityStandards;
    
    private String breachTerms;
    
    private String farmerSignUrl;
    
    private String purchaserSignUrl;
    
    private ContractStatus status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}