package cn.aspes.agri.trade.dto;

import cn.aspes.agri.trade.enums.ProductImageType;
import lombok.Data;

/**
 * 产品图片信息DTO
 */
@Data
public class ProductImageDTO {
    
    /**
     * 图片URL
     */
    private String url;
    
    /**
     * 图片类型
     */
    private ProductImageType imageType;
    
    /**
     * 排序
     */
    private Integer sort;
}