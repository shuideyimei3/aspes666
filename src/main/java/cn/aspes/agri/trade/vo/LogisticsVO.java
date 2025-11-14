package cn.aspes.agri.trade.vo;

import cn.aspes.agri.trade.enums.LogisticsStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流信息视图对象（包含轨迹）
 */
@Data
public class LogisticsVO {
    
    private Long id;
    
    private Long orderId;
    
    private String orderNo;  // 关联订单编号
    
    private String logisticsCompany;
    
    private String trackingNo;
    
    private String transportType;
    
    private LocalDateTime departureTime;
    
    private LocalDateTime arrivalTime;
    
    private LogisticsStatus status;
    
    private List<LogisticsTraceVO> traces;  // 物流轨迹列表
    
    private LocalDateTime createTime;
}
