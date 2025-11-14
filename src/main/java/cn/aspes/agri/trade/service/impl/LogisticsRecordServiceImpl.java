package cn.aspes.agri.trade.service.impl;

import cn.aspes.agri.trade.dto.LogisticsRequest;
import cn.aspes.agri.trade.dto.LogisticsTraceRequest;
import cn.aspes.agri.trade.entity.LogisticsRecord;
import cn.aspes.agri.trade.entity.LogisticsTrace;
import cn.aspes.agri.trade.entity.PurchaseOrder;
import cn.aspes.agri.trade.enums.LogisticsStatus;
import cn.aspes.agri.trade.enums.OrderStatus;
import cn.aspes.agri.trade.exception.BusinessException;
import cn.aspes.agri.trade.mapper.LogisticsRecordMapper;
import cn.aspes.agri.trade.service.LogisticsRecordService;
import cn.aspes.agri.trade.service.LogisticsTraceService;
import cn.aspes.agri.trade.service.PurchaseOrderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流记录服务实现
 */
@Slf4j
@Service
public class LogisticsRecordServiceImpl extends ServiceImpl<LogisticsRecordMapper, LogisticsRecord> implements LogisticsRecordService {
    
    @Resource
    private PurchaseOrderService purchaseOrderService;
    
    @Resource
    private LogisticsTraceService logisticsTraceService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLogistics(LogisticsRequest request) {
        // 检查订单是否存在
        PurchaseOrder order = purchaseOrderService.getById(request.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 检查是否已创建物流
        LogisticsRecord existing = getByOrderId(request.getOrderId());
        if (existing != null) {
            throw new BusinessException("该订单已创建物流记录");
        }
        
        // 创建物流记录
        LogisticsRecord logistics = new LogisticsRecord();
        BeanUtils.copyProperties(request, logistics);
        logistics.setStatus(LogisticsStatus.PENDING);
        
        save(logistics);
        return logistics.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipGoods(Long logisticsId) {
        LogisticsRecord logistics = getById(logisticsId);
        if (logistics == null) {
            throw new BusinessException("物流记录不存在");
        }
        
        if (logistics.getStatus() != LogisticsStatus.PENDING) {
            throw new BusinessException("物流状态异常，无法发货");
        }
        
        // 更新为已发货
        logistics.setStatus(LogisticsStatus.SHIPPED);
        logistics.setDepartureTime(LocalDateTime.now());
        updateById(logistics);
        
        // 添加物流轨迹
        LogisticsTrace trace = new LogisticsTrace();
        trace.setLogisticsId(logisticsId);
        trace.setNodeTime(LocalDateTime.now());
        trace.setNodeLocation("发货地");
        trace.setNodeDesc("货物已发出");
        logisticsTraceService.save(trace);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTrace(Long logisticsId, LogisticsTraceRequest request) {
        LogisticsRecord logistics = getById(logisticsId);
        if (logistics == null) {
            throw new BusinessException("物流记录不存在");
        }
        
        // 添加物流轨迹
        LogisticsTrace trace = new LogisticsTrace();
        BeanUtils.copyProperties(request, trace);
        trace.setLogisticsId(logisticsId);
        logisticsTraceService.save(trace);
        
        // 更新物流状态为运输中
        if (logistics.getStatus() == LogisticsStatus.SHIPPED) {
            logistics.setStatus(LogisticsStatus.TRANSIT);
            updateById(logistics);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceipt(Long logisticsId) {
        LogisticsRecord logistics = getById(logisticsId);
        if (logistics == null) {
            throw new BusinessException("物流记录不存在");
        }
        
        if (logistics.getStatus() == LogisticsStatus.SIGNED) {
            throw new BusinessException("已签收，请勿重复操作");
        }
        
        // 更新为已签收
        logistics.setStatus(LogisticsStatus.SIGNED);
        logistics.setArrivalTime(LocalDateTime.now());
        updateById(logistics);
        
        // ✅ 修复：预订签收后自动完成订单
        try {
            PurchaseOrder order = purchaseOrderService.getById(logistics.getOrderId());
            if (order != null && order.getStatus() == OrderStatus.PAID) {
                purchaseOrderService.completeOrder(logistics.getOrderId());
            }
        } catch (Exception e) {
            log.warn("物流签收时自动完成订单失败，物流={}", logisticsId, e);
        }
        
        // 添加签收轨迹
        LogisticsTrace trace = new LogisticsTrace();
        trace.setLogisticsId(logisticsId);
        trace.setNodeTime(LocalDateTime.now());
        trace.setNodeLocation("目的地");
        trace.setNodeDesc("货物已签收");
        logisticsTraceService.save(trace);
    }
    
    @Override
    public LogisticsRecord getByOrderId(Long orderId) {
        return getOne(new LambdaQueryWrapper<LogisticsRecord>()
                .eq(LogisticsRecord::getOrderId, orderId));
    }
    
    @Override
    public List<LogisticsTrace> listTraces(Long logisticsId) {
        return logisticsTraceService.list(new LambdaQueryWrapper<LogisticsTrace>()
                .eq(LogisticsTrace::getLogisticsId, logisticsId)
                .orderByAsc(LogisticsTrace::getNodeTime));
    }
    
    @Override
    public Page<LogisticsTrace> pageTraces(Long logisticsId, Integer current, Integer size) {
        Page<LogisticsTrace> page = new Page<>(current, size);
        return logisticsTraceService.page(page, new LambdaQueryWrapper<LogisticsTrace>()
                .eq(LogisticsTrace::getLogisticsId, logisticsId)
                .orderByDesc(LogisticsTrace::getNodeTime));
    }
}
