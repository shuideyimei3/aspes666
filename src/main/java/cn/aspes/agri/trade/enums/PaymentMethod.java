package cn.aspes.agri.trade.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付方式枚举
 */
@Getter
@AllArgsConstructor
public enum PaymentMethod {
    BANK_TRANSFER("bank_transfer", "银行转账"),
    ALIPAY("alipay", "支付宝"),
    WECHAT("wechat", "微信");
    
    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;
}
