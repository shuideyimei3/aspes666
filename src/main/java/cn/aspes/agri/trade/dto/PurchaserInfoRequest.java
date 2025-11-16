package cn.aspes.agri.trade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * 采购方信息提交请求
 */
@Data
public class PurchaserInfoRequest {
    
    @NotBlank(message = "企业名称不能为空")
    private String companyName;
    
    private String companyType;
    
    private MultipartFile businessLicenseFile;
    
    private String purchaseScale;
    
    private List<String> preferredOrigin;
    
    // 认证相关字段
    @NotBlank(message = "法定代表人不能为空")
    private String legalRepresentative;
    
    private String applyReason;
}