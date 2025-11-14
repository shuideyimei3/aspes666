package cn.aspes.agri.trade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 物流记录请求
 */
@Data
public class LogisticsRequest {
    
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
    
    @NotBlank(message = "物流公司不能为空")
    private String logisticsCompany;
    
    @NotBlank(message = "物流单号不能为空")
    private String trackingNo;
    
    private String transportType;
}
