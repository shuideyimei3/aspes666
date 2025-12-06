package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.entity.LogisticsTrace;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 物流轨迹服务
 */
public interface LogisticsTraceService extends IService<LogisticsTrace> {
    
    /**
     * 查询物流轨迹列表
     */
    List<LogisticsTrace> listByLogisticsId(Long logisticsId);
}
