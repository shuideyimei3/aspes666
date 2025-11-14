package cn.aspes.agri.trade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

/**
 * 采购需求发布请求
 */
@Data
public class PurchaseDemandRequest {
    
    @NotNull(message = "产品分类不能为空")
    private Long categoryId;
    
    @NotBlank(message = "产品名称不能为空")
    private String productName;
    
    private String specRequire;
    
    @NotNull(message = "需求数量不能为空")
    @Positive(message = "需求数量必须大于0")
    private Integer quantity;
    
    @NotBlank(message = "单位不能为空")
    private String unit;
    
    private String priceRange;
    
    @NotNull(message = "期望交货日期不能为空")
    private LocalDate deliveryDate;
    
    @NotBlank(message = "交货地点不能为空")
    private String deliveryAddress;
    
    private String qualityRequire;
}
