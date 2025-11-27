package cn.aspes.agri.trade.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 库存预留状态
 */
@Getter
@AllArgsConstructor
public enum ReservationStatus {
    /** 已预留，待支付/待确认 */
    PENDING("pending", "已预留"),
    /** 已确认（支付成功） */
    CONFIRMED("confirmed", "已确认"),
    /** 主动释放，例如订单取消、支付失败 */
    RELEASED("released", "已释放"),
    /** 预留超时自动过期 */
    EXPIRED("expired", "已过期");
    
    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;
}
