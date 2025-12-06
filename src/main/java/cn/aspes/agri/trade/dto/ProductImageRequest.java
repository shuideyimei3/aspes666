package cn.aspes.agri.trade.dto;

import cn.aspes.agri.trade.enums.ProductImageType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 产品图片信息请求DTO
 */
@Data
public class ProductImageRequest {
    
    /**
     * 图片文件
     */
    @NotNull(message = "图片文件不能为空")
    private MultipartFile file;
    
    /**
     * 图片类型
     */
    @NotNull(message = "图片类型不能为空")
    private ProductImageType imageType;
    
    /**
     * 图片排序
     */
    private Integer sort;
}