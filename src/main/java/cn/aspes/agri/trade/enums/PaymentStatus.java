package cn.aspes.agri.trade.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付状态枚举
 */
@Getter
@AllArgsConstructor
public enum PaymentStatus {
    PENDING("pending", "待支付"),
    SUCCESS("success", "支付成功"),
    FAILED("failed", "支付失败");
    
    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;
}
