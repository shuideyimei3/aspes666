package cn.aspes.agri.trade.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 物流状态枚举
 */
@Getter
@AllArgsConstructor
public enum LogisticsStatus {
    PENDING("pending", "待发货"),
    SHIPPED("shipped", "已发货"),
    TRANSIT("transit", "运输中"),
    ARRIVED("arrived", "已到达"),
    SIGNED("signed", "已签收");
    
    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;
}
