package cn.aspes.agri.trade.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
@AllArgsConstructor
public enum UserRole {
    FARMER("farmer", "农户"),
    PURCHASER("purchaser", "采购方"),
    ADMIN("admin", "管理员");
    
    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;
}
