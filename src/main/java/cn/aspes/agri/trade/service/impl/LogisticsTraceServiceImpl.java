package cn.aspes.agri.trade.service.impl;

import cn.aspes.agri.trade.entity.LogisticsTrace;
import cn.aspes.agri.trade.mapper.LogisticsTraceMapper;
import cn.aspes.agri.trade.service.LogisticsTraceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 物流轨迹服务实现
 */
@Service
public class LogisticsTraceServiceImpl extends ServiceImpl<LogisticsTraceMapper, LogisticsTrace> implements LogisticsTraceService {
    
    @Override
    public List<LogisticsTrace> listByLogisticsId(Long logisticsId) {
        return list(new LambdaQueryWrapper<LogisticsTrace>()
                .eq(LogisticsTrace::getLogisticsId, logisticsId)
                .orderByAsc(LogisticsTrace::getNodeTime));
    }
}
