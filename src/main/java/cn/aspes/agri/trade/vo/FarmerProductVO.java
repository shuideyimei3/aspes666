package cn.aspes.agri.trade.vo;

import cn.aspes.agri.trade.enums.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 农户产品视图对象
 */
@Data
public class FarmerProductVO {
    
    private Long id;
    
    private Long farmerId;
    
    private String farmName;  // 关联农场名称
    
    private Long categoryId;
    
    private String categoryName;  // 关联分类名称
    
    private String name;
    
    private String spec;
    
    private String unit;
    
    private BigDecimal price;
    
    private Integer minPurchase;
    
    private Integer stock;
    
    private LocalDate productionDate;
    
    private String shelfLife;
    
    private String productionMethod;
    
    private Integer originAreaId;
    
    private String originAreaName;  // 关联产地名称
    
    private String description;
    
    private ProductStatus status;
    
    private List<String> images;  // 产品图片列表
    
    private LocalDateTime createTime;
}
