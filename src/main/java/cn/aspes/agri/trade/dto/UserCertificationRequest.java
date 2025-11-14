package cn.aspes.agri.trade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户认证申请请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCertificationRequest {
    
    /**
     * 申请类型：farmer(农户) purchaser(采购方)
     */
    @NotBlank(message = "申请类型不能为空")
    private String applyType;
    
    /**
     * 身份证号或营业执照号
     */
    @NotBlank(message = "证件号码不能为空")
    private String idNumber;
    
    /**
     * 身份证正面照URL
     */
    @NotBlank(message = "身份证正面照不能为空")
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
}
