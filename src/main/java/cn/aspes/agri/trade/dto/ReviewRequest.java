package cn.aspes.agri.trade.dto;

import cn.aspes.agri.trade.enums.UserRole;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评价请求
 */
@Data
public class ReviewRequest {
    
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
    
    @NotNull(message = "被评价方角色不能为空")
    private UserRole reviewTo;
    
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分不能低于1星")
    @Max(value = 5, message = "评分不能超过5星")
    private Integer rating;
    
    private String comment;
}
