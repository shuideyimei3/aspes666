package cn.aspes.agri.trade.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 订单发货请求
 */
@Data
public class OrderDeliveryRequest {
    
    @NotNull(message = "实际数量不能为空")
    @Positive(message = "实际数量必须大于0")
    private Integer actualQuantity;
    
    private String inspectionResult;
}
