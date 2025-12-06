package cn.aspes.agri.trade.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 合同状态枚举
 */
@Getter
@AllArgsConstructor
public enum ContractStatus {
    DRAFT("draft", "草稿"),
    SIGNED("signed", "已签署"),
    EXECUTING("executing", "执行中"),
    COMPLETED("completed", "已完成"),
    TERMINATED("terminated", "已终止");
    
    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;
}
