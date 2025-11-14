package cn.aspes.agri.trade.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 对接状态枚举
 */
@Getter
@AllArgsConstructor
public enum DockingStatus {
    PENDING("pending", "待处理"),
    NEGOTIATING("negotiating", "协商中"),
    AGREED("agreed", "已达成"),
    REJECTED("rejected", "已拒绝");
    
    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;
}
