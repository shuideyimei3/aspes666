package cn.aspes.agri.trade.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流轨迹添加请求
 */
@Data
public class LogisticsTraceRequest {
    
    @NotNull(message = "节点时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nodeTime;
    
    private String nodeLocation;
    
    @NotBlank(message = "节点描述不能为空")
    private String nodeDesc;
}
