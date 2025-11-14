package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.dto.LogisticsRequest;
import cn.aspes.agri.trade.dto.LogisticsTraceRequest;
import cn.aspes.agri.trade.entity.LogisticsRecord;
import cn.aspes.agri.trade.entity.LogisticsTrace;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 物流记录服务
 */
public interface LogisticsRecordService extends IService<LogisticsRecord> {
    
    /**
     * 创建物流记录
     */
    Long createLogistics(LogisticsRequest request);
    
    /**
     * 发货
     */
    void shipGoods(Long logisticsId);
    
    /**
     * 添加物流轨迹
     */
    void addTrace(Long logisticsId, LogisticsTraceRequest request);
    
    /**
     * 确认签收
     */
    void confirmReceipt(Long logisticsId);
    
    /**
     * 查询订单物流信息
     */
    LogisticsRecord getByOrderId(Long orderId);
    
    /**
     * 查询物流轨迹列表
     */
    List<LogisticsTrace> listTraces(Long logisticsId);
    
    /**
     * 分页查询物流轨迹
     */
    Page<LogisticsTrace> pageTraces(Long logisticsId, Integer current, Integer size);
}
