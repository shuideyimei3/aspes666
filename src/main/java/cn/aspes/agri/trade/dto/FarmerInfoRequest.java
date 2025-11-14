package cn.aspes.agri.trade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 农户信息提交请求
 */
@Data
public class FarmerInfoRequest {
    
    @NotBlank(message = "农场名称不能为空")
    private String farmName;
    
    @NotNull(message = "产地不能为空")
    private Integer originAreaId;
    
    private String productionScale;
    
    private Map<String, String> certifications;
    
    private String bankAccount;
    
    private String bankName;
}
