package cn.aspes.agri.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流轨迹实体类
 */
@Data
@TableName("logistics_trace")
public class LogisticsTrace {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long logisticsId;
    
    private LocalDateTime nodeTime;
    
    private String nodeLocation;
    
    private String nodeDesc;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
