package cn.aspes.agri.trade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    
    // 农户交货相关字段
    @NotNull(message = "实际交货数量不能为空")
    @Positive(message = "实际交货数量必须大于0")
    private Integer actualQuantity;
    
    @NotBlank(message = "检验结果不能为空")
    private String inspectionResult;
}