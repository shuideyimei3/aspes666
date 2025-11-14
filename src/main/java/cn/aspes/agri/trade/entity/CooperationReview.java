package cn.aspes.agri.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 合作评价实体类
 */
@Data
@TableName("cooperation_review")
public class CooperationReview {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long orderId;
    
    private String reviewFrom;
    
    private String reviewTo;
    
    private Long targetId;
    
    private Integer rating;
    
    private String comment;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
