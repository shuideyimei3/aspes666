package cn.aspes.agri.trade.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
@AllArgsConstructor
public enum OrderStatus {
    PENDING("pending", "待交货"),
    DELIVERED("delivered", "已交货"),
    PAID("paid", "已支付"),
    COMPLETED("completed", "已完成"),
    CANCELLED("cancelled", "已取消");
    
    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;
}
