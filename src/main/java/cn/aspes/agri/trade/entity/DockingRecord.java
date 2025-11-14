package cn.aspes.agri.trade.entity;

import cn.aspes.agri.trade.enums.DockingStatus;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 对接记录实体类
 */
@Data
@TableName("docking_record")
public class DockingRecord {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long demandId;
    
    private Long farmerId;
    
    private Long productId;
    
    private BigDecimal quotePrice;
    
    private Integer canSupply;
    
    private LocalDate supplyTime;
    
    private String contactWay;
    
    private String remark;
    
    private DockingStatus status;
    
    private String purchaserRemark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
