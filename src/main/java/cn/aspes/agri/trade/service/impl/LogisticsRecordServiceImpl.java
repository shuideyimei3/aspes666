package cn.aspes.agri.trade.service.impl;

import cn.aspes.agri.trade.dto.LogisticsRequest;
import cn.aspes.agri.trade.dto.LogisticsTraceRequest;
import cn.aspes.agri.trade.entity.FarmerInfo;
import cn.aspes.agri.trade.entity.LogisticsRecord;
import cn.aspes.agri.trade.entity.LogisticsTrace;
import cn.aspes.agri.trade.entity.PurchaseOrder;
import cn.aspes.agri.trade.entity.PurchaserInfo;
import cn.aspes.agri.trade.enums.LogisticsStatus;
import cn.aspes.agri.trade.enums.OrderStatus;
import cn.aspes.agri.trade.exception.BusinessException;
import cn.aspes.agri.trade.mapper.LogisticsRecordMapper;
import cn.aspes.agri.trade.service.FarmerInfoService;
import cn.aspes.agri.trade.service.LogisticsRecordService;
import cn.aspes.agri.trade.service.LogisticsTraceService;
import cn.aspes.agri.trade.service.PurchaseOrderService;
import cn.aspes.agri.trade.service.PurchaserInfoService;
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
    
    @Resource
    private FarmerInfoService farmerInfoService;
    
    @Resource
    private PurchaserInfoService purchaserInfoService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLogistics(LogisticsRequest request, Long userId) {
        // 检查订单是否存在
        PurchaseOrder order = purchaseOrderService.getById(request.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 验证用户权限：只有农户或采购方可以创建物流记录
        FarmerInfo farmer = farmerInfoService.getByUserId(userId);
        PurchaserInfo purchaser = purchaserInfoService.getByUserId(userId);
        
        if (farmer == null && purchaser == null) {
            throw new BusinessException("无权限创建物流记录");
        }
        
        // 如果是农户，验证是否是该订单的农户
        if (farmer != null && !farmer.getId().equals(order.getFarmerId())) {
            throw new BusinessException("无权限为该订单创建物流记录");
        }
        
        // 如果是采购方，验证是否是该订单的采购方
        if (purchaser != null && !purchaser.getId().equals(order.getPurchaserId())) {
            throw new BusinessException("无权限为该订单创建物流记录");
        }
        
        // 允许一个订单创建多个物流记录
        
        // 执行农户交货操作
        purchaseOrderService.deliverOrder(request.getOrderId(), request.getActualQuantity(), request.getInspectionResult());
        
        // 创建物流记录
        LogisticsRecord logistics = new LogisticsRecord();
        BeanUtils.copyProperties(request, logistics);
        logistics.setStatus(LogisticsStatus.PENDING);
        
        save(logistics);
        
        // 自动执行发货操作
        shipGoods(logistics.getId());
        
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
    public void addTrace(Long logisticsId, LogisticsTraceRequest request, Long userId) {
        LogisticsRecord logistics = getById(logisticsId);
        if (logistics == null) {
            throw new BusinessException("物流记录不存在");
        }
        
        // 获取订单信息
        PurchaseOrder order = purchaseOrderService.getById(logistics.getOrderId());
        if (order == null) {
            throw new BusinessException("关联订单不存在");
        }
        
        // 验证用户权限：只有农户、采购方或管理员可以添加物流轨迹
        FarmerInfo farmer = farmerInfoService.getByUserId(userId);
        PurchaserInfo purchaser = purchaserInfoService.getByUserId(userId);
        
        if (farmer == null && purchaser == null) {
            throw new BusinessException("无权限添加物流轨迹");
        }
        
        // 如果是农户，验证是否是该订单的农户
        if (farmer != null && !farmer.getId().equals(order.getFarmerId())) {
            throw new BusinessException("无权限为该订单添加物流轨迹");
        }
        
        // 如果是采购方，验证是否是该订单的采购方
        if (purchaser != null && !purchaser.getId().equals(order.getPurchaserId())) {
            throw new BusinessException("无权限为该订单添加物流轨迹");
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
    public void confirmReceipt(Long logisticsId, Long userId) {
        LogisticsRecord logistics = getById(logisticsId);
        if (logistics == null) {
            throw new BusinessException("物流记录不存在");
        }
        
        if (logistics.getStatus() == LogisticsStatus.SIGNED) {
            throw new BusinessException("已签收，请勿重复操作");
        }
        
        // 获取订单信息
        PurchaseOrder order = purchaseOrderService.getById(logistics.getOrderId());
        if (order == null) {
            throw new BusinessException("关联订单不存在");
        }
        
        // 验证用户权限：只有采购方可以确认签收
        PurchaserInfo purchaser = purchaserInfoService.getByUserId(userId);
        if (purchaser == null) {
            throw new BusinessException("无权限确认签收");
        }
        
        // 验证是否是该订单的采购方
        if (!purchaser.getId().equals(order.getPurchaserId())) {
            throw new BusinessException("无权限为该订单确认签收");
        }
        
        // 更新为已签收
        logistics.setStatus(LogisticsStatus.SIGNED);
        logistics.setArrivalTime(LocalDateTime.now());
        updateById(logistics);
        
        // ✅ 修改：移除确认签收后自动完成订单的逻辑
        // 确认签收不应该将订单状态更新为已完成，需要手动完成订单
        
        // 添加签收轨迹
        LogisticsTrace trace = new LogisticsTrace();
        trace.setLogisticsId(logisticsId);
        trace.setNodeTime(LocalDateTime.now());
        trace.setNodeLocation("目的地");
        trace.setNodeDesc("货物已签收");
        logisticsTraceService.save(trace);
    }
    
    @Override
    public LogisticsRecord getByOrderId(Long orderId, Long userId) {
        // 获取订单信息
        PurchaseOrder order = purchaseOrderService.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 验证用户权限：只有农户、采购方或管理员可以查看物流信息
        FarmerInfo farmer = farmerInfoService.getByUserId(userId);
        PurchaserInfo purchaser = purchaserInfoService.getByUserId(userId);
        
        if (farmer == null && purchaser == null) {
            throw new BusinessException("无权限查看物流信息");
        }
        
        // 如果是农户，验证是否是该订单的农户
        if (farmer != null && !farmer.getId().equals(order.getFarmerId())) {
            throw new BusinessException("无权限查看该订单的物流信息");
        }
        
        // 如果是采购方，验证是否是该订单的采购方
        if (purchaser != null && !purchaser.getId().equals(order.getPurchaserId())) {
            throw new BusinessException("无权限查看该订单的物流信息");
        }
        
        return getOne(new LambdaQueryWrapper<LogisticsRecord>()
                .eq(LogisticsRecord::getOrderId, orderId)
                .orderByDesc(LogisticsRecord::getCreateTime)
                .last("LIMIT 1"));
    }
    
    @Override
    public List<LogisticsRecord> listByOrderId(Long orderId, Long userId) {
        // 获取订单信息
        PurchaseOrder order = purchaseOrderService.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 验证用户权限：只有农户、采购方或管理员可以查看物流信息
        FarmerInfo farmer = farmerInfoService.getByUserId(userId);
        PurchaserInfo purchaser = purchaserInfoService.getByUserId(userId);
        
        if (farmer == null && purchaser == null) {
            throw new BusinessException("无权限查看物流信息");
        }
        
        // 如果是农户，验证是否是该订单的农户
        if (farmer != null && !farmer.getId().equals(order.getFarmerId())) {
            throw new BusinessException("无权限查看该订单的物流信息");
        }
        
        // 如果是采购方，验证是否是该订单的采购方
        if (purchaser != null && !purchaser.getId().equals(order.getPurchaserId())) {
            throw new BusinessException("无权限查看该订单的物流信息");
        }
        
        return list(new LambdaQueryWrapper<LogisticsRecord>()
                .eq(LogisticsRecord::getOrderId, orderId)
                .orderByDesc(LogisticsRecord::getCreateTime));
    }
    
    @Override
    public List<LogisticsTrace> listTraces(Long logisticsId, Long userId) {
        LogisticsRecord logistics = getById(logisticsId);
        if (logistics == null) {
            throw new BusinessException("物流记录不存在");
        }
        
        // 获取订单信息
        PurchaseOrder order = purchaseOrderService.getById(logistics.getOrderId());
        if (order == null) {
            throw new BusinessException("关联订单不存在");
        }
        
        // 验证用户权限：只有农户、采购方或管理员可以查看物流轨迹
        FarmerInfo farmer = farmerInfoService.getByUserId(userId);
        PurchaserInfo purchaser = purchaserInfoService.getByUserId(userId);
        
        if (farmer == null && purchaser == null) {
            throw new BusinessException("无权限查看物流轨迹");
        }
        
        // 如果是农户，验证是否是该订单的农户
        if (farmer != null && !farmer.getId().equals(order.getFarmerId())) {
            throw new BusinessException("无权限查看该订单的物流轨迹");
        }
        
        // 如果是采购方，验证是否是该订单的采购方
        if (purchaser != null && !purchaser.getId().equals(order.getPurchaserId())) {
            throw new BusinessException("无权限查看该订单的物流轨迹");
        }
        
        return logisticsTraceService.list(new LambdaQueryWrapper<LogisticsTrace>()
                .eq(LogisticsTrace::getLogisticsId, logisticsId)
                .orderByAsc(LogisticsTrace::getNodeTime));
    }
    
    @Override
    public Page<LogisticsTrace> pageTraces(Long logisticsId, Integer current, Integer size, Long userId) {
        LogisticsRecord logistics = getById(logisticsId);
        if (logistics == null) {
            throw new BusinessException("物流记录不存在");
        }
        
        // 获取订单信息
        PurchaseOrder order = purchaseOrderService.getById(logistics.getOrderId());
        if (order == null) {
            throw new BusinessException("关联订单不存在");
        }
        
        // 验证用户权限：只有农户、采购方或管理员可以查看物流轨迹
        FarmerInfo farmer = farmerInfoService.getByUserId(userId);
        PurchaserInfo purchaser = purchaserInfoService.getByUserId(userId);
        
        if (farmer == null && purchaser == null) {
            throw new BusinessException("无权限查看物流轨迹");
        }
        
        // 如果是农户，验证是否是该订单的农户
        if (farmer != null && !farmer.getId().equals(order.getFarmerId())) {
            throw new BusinessException("无权限查看该订单的物流轨迹");
        }
        
        // 如果是采购方，验证是否是该订单的采购方
        if (purchaser != null && !purchaser.getId().equals(order.getPurchaserId())) {
            throw new BusinessException("无权限查看该订单的物流轨迹");
        }
        
        Page<LogisticsTrace> page = new Page<>(current, size);
        return logisticsTraceService.page(page, new LambdaQueryWrapper<LogisticsTrace>()
                .eq(LogisticsTrace::getLogisticsId, logisticsId)
                .orderByDesc(LogisticsTrace::getNodeTime));
    }
}