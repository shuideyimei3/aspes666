package cn.aspes.agri.trade.vo;

import cn.aspes.agri.trade.enums.DemandStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 采购需求视图对象
 */
@Data
public class PurchaseDemandVO {
    
    private Long id;
    
    private Long purchaserId;
    
    private String companyName;  // 关联采购方公司名称
    
    private Long categoryId;
    
    private String categoryName;  // 关联分类名称
    
    private String productName;
    
    private String specRequire;
    
    private Integer quantity;
    
    private String unit;
    
    private String priceRange;
    
    private LocalDate deliveryDate;
    
    private String deliveryAddress;
    
    private String qualityRequire;
    
    private DemandStatus status;
    
    private Integer dockingCount;  // 对接响应数量
    
    private LocalDateTime createTime;
}
