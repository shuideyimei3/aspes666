package cn.aspes.agri.trade.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 对接记录请求（农户响应需求）
 */
@Data
public class DockingRecordRequest {
    
    @NotNull(message = "需求ID不能为空")
    private Long demandId;
    
    private Long productId;
    
    @NotNull(message = "报价不能为空")
    @Positive(message = "报价必须大于0")
    private BigDecimal quotePrice;
    
    @NotNull(message = "可供应数量不能为空")
    @Positive(message = "可供应数量必须大于0")
    private Integer canSupply;
    
    private LocalDate supplyTime;
    
    private String contactWay;
    
    private String remark;
}
