package cn.aspes.agri.trade.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 产品状态枚举
 */
@Getter
@AllArgsConstructor
public enum ProductStatus {
    ON_SALE("on_sale", "在售"),
    OFF_SALE("off_sale", "下架");
    
    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;
}
