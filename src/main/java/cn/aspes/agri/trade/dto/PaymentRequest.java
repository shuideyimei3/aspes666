package cn.aspes.agri.trade.dto;

import cn.aspes.agri.trade.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

/**
 * 支付请求（支持文件上传）
 */
@Data
public class PaymentRequest {
    
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
    
    @NotBlank(message = "支付阶段不能为空")
    private String paymentStage;
    
    @NotNull(message = "支付金额不能为空")
    @Positive(message = "支付金额必须大于0")
    private BigDecimal amount;
    
    @NotNull(message = "支付方式不能为空")
    private PaymentMethod paymentMethod;
    
    // 支付凭证文件，可选，会自动上传
    private MultipartFile voucherFile;
}