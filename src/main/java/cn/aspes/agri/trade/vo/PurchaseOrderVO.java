package cn.aspes.agri.trade.vo;

import cn.aspes.agri.trade.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 采购订单视图对象
 */
@Data
public class PurchaseOrderVO {
    
    private Long id;
    
    private String orderNo;
    
    private Long contractId;
    
    private String contractNo;  // 关联合同编号
    
    private Map<String, Object> productInfo;
    
    private Integer actualQuantity;
    
    private BigDecimal actualAmount;
    
    private OrderStatus status;
    
    private LocalDateTime deliveryTime;
    
    private String inspectionResult;
    
    private LocalDateTime createTime;
}
