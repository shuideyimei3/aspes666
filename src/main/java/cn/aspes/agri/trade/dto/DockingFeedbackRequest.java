package cn.aspes.agri.trade.dto;

import cn.aspes.agri.trade.enums.DockingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 对接反馈请求（采购方处理）
 */
@Data
public class DockingFeedbackRequest {
    
    @NotNull(message = "对接状态不能为空")
    private DockingStatus status;
    
    private String purchaserRemark;
}
