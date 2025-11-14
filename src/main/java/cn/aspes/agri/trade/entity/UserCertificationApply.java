package cn.aspes.agri.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户认证申请表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_certification_apply")
public class UserCertificationApply {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 申请用户ID
     */
    private Long userId;
    
    /**
     * 申请类型：farmer(农户) purchaser(采购方)
     */
    private String applyType;
    
    /**
     * 身份证号或营业执照号
     */
    private String idNumber;
    
    /**
     * 身份证正面照URL
     */
    private String idCardFrontUrl;
    
    /**
     * 身份证反面照URL
     */
    private String idCardBackUrl;
    
    /**
     * 营业执照照片URL
     */
    private String businessLicenseUrl;
    
    /**
     * 法定代表人
     */
    private String legalRepresentative;
    
    /**
     * 申请说明
     */
    private String applyReason;
    
    /**
     * 申请状态：pending(待审核) approved(已批准) rejected(已拒绝)
     */
    private String status;
    
    /**
     * 管理员备注
     */
    private String adminRemark;
    
    /**
     * 批准时间
     */
    private LocalDateTime approvedTime;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
