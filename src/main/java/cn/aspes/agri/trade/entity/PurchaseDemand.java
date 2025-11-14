package cn.aspes.agri.trade.entity;

import cn.aspes.agri.trade.enums.DemandStatus;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 采购需求实体类
 */
@Data
@TableName("purchase_demand")
public class PurchaseDemand {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long purchaserId;
    
    private Long categoryId;
    
    private String productName;
    
    private String specRequire;
    
    private Integer quantity;
    
    private String unit;
    
    private String priceRange;
    
    private LocalDate deliveryDate;
    
    private String deliveryAddress;
    
    private String qualityRequire;
    
    private DemandStatus status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
