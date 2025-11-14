package cn.aspes.agri.trade.entity;

import cn.aspes.agri.trade.enums.LogisticsStatus;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流记录实体类
 */
@Data
@TableName("logistics_record")
public class LogisticsRecord {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long orderId;
    
    private String logisticsCompany;
    
    private String trackingNo;
    
    private String transportType;
    
    private LocalDateTime departureTime;
    
    private LocalDateTime arrivalTime;
    
    private LogisticsStatus status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
