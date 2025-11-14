package cn.aspes.agri.trade.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 需求状态枚举
 */
@Getter
@AllArgsConstructor
public enum DemandStatus {
    PENDING("pending", "待匹配"),
    MATCHED("matched", "已匹配"),
    CLOSED("closed", "已关闭");
    
    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;
}
