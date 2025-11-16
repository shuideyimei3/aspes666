package cn.aspes.agri.trade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;

/**
 * 合同创建/签署请求（支持文件上传）
 */
@Data
public class ContractRequest {
    
    @NotNull(message = "对接记录ID不能为空")
    private Long dockingId;
    
    @NotNull(message = "产品ID不能为空")
    private Long productId;
    
    @NotNull(message = "产品数量不能为空")
    private Integer quantity;
    
    @NotBlank(message = "付款方式不能为空")
    private String paymentTerms;
    
    @NotNull(message = "交货时间不能为空")
    private LocalDate deliveryTime;
    
    @NotBlank(message = "交货地址不能为空")
    private String deliveryAddress;
    
    private String qualityStandards;
    
    private String breachTerms;
    
    // 合同签字文件，签署时必填
    private MultipartFile signFile;
}