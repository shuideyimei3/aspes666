package cn.aspes.agri.trade.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流轨迹视图对象
 */
@Data
public class LogisticsTraceVO {
    
    private Long id;
    
    private LocalDateTime nodeTime;
    
    private String nodeLocation;
    
    private String nodeDesc;
}
