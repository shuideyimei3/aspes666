package cn.aspes.agri.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 库存预留表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("stock_reservation")
public class StockReservation {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 产品ID
     */
    private Long productId;
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 预留数量
     */
    private Integer reservedQuantity;
    
    /**
     * 预留状态：reserved(已预留) released(已释放) expired(已过期)
     */
    private String status;
    
    /**
     * 释放原因
     */
    private String releaseReason;
    
    /**
     * 预留过期时间
     */
    private LocalDateTime expiredTime;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
