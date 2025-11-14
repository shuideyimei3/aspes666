package cn.aspes.agri.trade.vo;

import cn.aspes.agri.trade.enums.ContractStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 采购合同视图对象
 */
@Data
public class PurchaseContractVO {
    
    private Long id;
    
    private String contractNo;
    
    private Long dockingId;
    
    private Long purchaserId;
    
    private String companyName;  // 采购方公司名称
    
    private Long farmerId;
    
    private String farmName;  // 农场名称
    
    private Map<String, Object> productInfo;
    
    private BigDecimal totalAmount;
    
    private String paymentTerms;
    
    private LocalDate deliveryTime;
    
    private String deliveryAddress;
    
    private String qualityStandards;
    
    private String breachTerms;
    
    private String farmerSignUrl;
    
    private String purchaserSignUrl;
    
    private ContractStatus status;
    
    private LocalDateTime createTime;
}
