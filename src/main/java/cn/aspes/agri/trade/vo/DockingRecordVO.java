package cn.aspes.agri.trade.vo;

import cn.aspes.agri.trade.enums.DockingStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 对接记录视图对象
 */
@Data
public class DockingRecordVO {
    
    private Long id;
    
    private Long demandId;
    
    private String demandProductName;  // 需求产品名称
    
    private Long farmerId;
    
    private String farmName;  // 农场名称
    
    private String farmerContact;  // 农户联系方式
    
    private Long productId;
    
    private String productName;  // 产品名称
    
    private BigDecimal quotePrice;
    
    private Integer canSupply;
    
    private LocalDate supplyTime;
    
    private String contactWay;
    
    private String remark;
    
    private DockingStatus status;
    
    private String purchaserRemark;
    
    private String purchaserName;  // 采购商公司名称
    
    private LocalDateTime createTime;
}