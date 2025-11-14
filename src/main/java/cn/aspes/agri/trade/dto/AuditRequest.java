package cn.aspes.agri.trade.dto;

import cn.aspes.agri.trade.enums.AuditStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审核请求
 */
@Data
public class AuditRequest {
    
    @NotNull(message = "审核状态不能为空")
    private AuditStatus auditStatus;
    
    private String auditRemark;
}
