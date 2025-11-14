package cn.aspes.agri.trade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 农户产品发布/编辑请求（支持文件上传）
 */
@Data
public class FarmerProductRequest {
    
    @NotNull(message = "产品分类不能为空")
    private Long categoryId;
    
    @NotBlank(message = "产品名称不能为空")
    private String name;
    
    @NotBlank(message = "规格不能为空")
    private String spec;
    
    @NotBlank(message = "单位不能为空")
    private String unit;
    
    @NotNull(message = "价格不能为空")
    @Positive(message = "价格必须大于0")
    private BigDecimal price;
    
    @NotNull(message = "起订量不能为空")
    @Positive(message = "起订量必须大于0")
    private Integer minPurchase;
    
    @NotNull(message = "库存不能为空")
    @Positive(message = "库存必须大于0")
    private Integer stock;
    
    private LocalDate productionDate;
    
    private String shelfLife;
    
    private String productionMethod;
    
    @NotNull(message = "产地不能为空")
    private Integer originAreaId;
    
    private String description;
    
    // 产品图片，新增时需要至少一张，修改时可选
    private List<MultipartFile> productImages;
}
