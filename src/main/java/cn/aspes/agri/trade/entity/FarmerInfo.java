package cn.aspes.agri.trade.entity;

import cn.aspes.agri.trade.enums.AuditStatus;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 农户信息实体类
 */
@Data
@TableName(value = "farmer_info", autoResultMap = true)
public class FarmerInfo {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long userId;
    
    private String farmName;
    
    private Integer originAreaId;
    
    private String productionScale;
    
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> certifications;
    
    private String bankAccount;
    
    private String bankName;
    
    // 认证相关字段
    private String idNumber;
    
    private String idCardFrontUrl;
    
    private String idCardBackUrl;
    
    private String applyReason;
    
    private AuditStatus auditStatus;
    
    private String auditRemark;
    
    private LocalDateTime approvedTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}