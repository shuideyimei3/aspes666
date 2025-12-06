package cn.aspes.agri.trade.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 产品图片类型枚举
 */
@Getter
public enum ProductImageType {
    
    COVER("cover", "封面图"),
    PRODUCTION("production", "生产场景图"),
    DETAIL("detail", "细节图");
    
    @EnumValue
    @JsonValue
    private final String code;
    
    private final String description;
    
    ProductImageType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
