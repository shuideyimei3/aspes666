package cn.aspes.agri.trade.entity;

import cn.aspes.agri.trade.enums.ProductStatus;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 农户产品实体类
 */
@Data
@TableName("farmer_product")
public class FarmerProduct {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long farmerId;
    
    private Long categoryId;
    
    private String name;
    
    private String spec;
    
    private String unit;
    
    private BigDecimal price;
    
    private Integer minPurchase;
    
    private Integer stock;
    
    private LocalDate productionDate;
    
    private String shelfLife;
    
    private String productionMethod;
    
    private Integer originAreaId;
    
    private String description;
    
    private ProductStatus status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
