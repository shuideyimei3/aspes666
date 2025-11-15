package cn.aspes.agri.trade.entity;

import cn.aspes.agri.trade.enums.AuditStatus;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 采购方信息实体类
 */
@Data
@TableName(value = "purchaser_info", autoResultMap = true)
public class PurchaserInfo {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long userId;
    
    private String companyName;
    
    private String companyType;
    
    private String businessLicense;
    
    private String purchaseScale;
    
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> preferredOrigin;
    
    // 认证相关字段
    private String legalRepresentative;
    
    private String applyReason;
    
    private AuditStatus auditStatus;
    
    private String auditRemark;
    
    private LocalDateTime approvedTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}