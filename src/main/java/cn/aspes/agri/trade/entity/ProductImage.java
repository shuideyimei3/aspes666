package cn.aspes.agri.trade.entity;

import cn.aspes.agri.trade.enums.ProductImageType;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 产品图片实体类
 */
@Data
@TableName("product_image")
public class ProductImage {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long productId;
    
    private String imageUrl;
    
    private ProductImageType imageType;
    
    private Integer sort;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
