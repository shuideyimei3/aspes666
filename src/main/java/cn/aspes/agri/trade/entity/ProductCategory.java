package cn.aspes.agri.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 产品分类实体类
 */
@Data
@TableName(value = "product_category", autoResultMap = true)
public class ProductCategory {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private String name;
    
    private Long parentId;
    
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> attribute;
    
    private String status;
    
    private LocalDateTime createTime;
}