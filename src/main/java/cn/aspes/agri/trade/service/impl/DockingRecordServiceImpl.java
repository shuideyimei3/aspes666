package cn.aspes.agri.trade.service.impl;

import cn.aspes.agri.trade.dto.DockingFeedbackRequest;
import cn.aspes.agri.trade.dto.DockingRecordRequest;
import cn.aspes.agri.trade.entity.DockingRecord;
import cn.aspes.agri.trade.entity.FarmerInfo;
import cn.aspes.agri.trade.entity.PurchaseDemand;
import cn.aspes.agri.trade.entity.PurchaserInfo;
import cn.aspes.agri.trade.enums.DemandStatus;
import cn.aspes.agri.trade.enums.DockingStatus;
import cn.aspes.agri.trade.exception.BusinessException;
import cn.aspes.agri.trade.mapper.DockingRecordMapper;
import cn.aspes.agri.trade.service.DockingRecordService;
import cn.aspes.agri.trade.service.FarmerInfoService;
import cn.aspes.agri.trade.service.PurchaseDemandService;
import cn.aspes.agri.trade.service.PurchaserInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 对接记录服务实现
 */
@Service
public class DockingRecordServiceImpl extends ServiceImpl<DockingRecordMapper, DockingRecord> implements DockingRecordService {
    
    @Resource
    private PurchaseDemandService purchaseDemandService;
    
    @Resource
    private FarmerInfoService farmerInfoService;
    
    @Resource
    private PurchaserInfoService purchaserInfoService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long respondToDemand(Long farmerId, DockingRecordRequest request) {
        // 检查农户是否存在
        FarmerInfo farmerInfo = farmerInfoService.getById(farmerId);
        if (farmerInfo == null) {
            throw new BusinessException("农户信息不存在");
        }
        
        // 检查需求是否存在且状态为待匹配
        PurchaseDemand demand = purchaseDemandService.getById(request.getDemandId());
        if (demand == null) {
            throw new BusinessException("采购需求不存在");
        }
        if (demand.getStatus() != DemandStatus.PENDING) {
            throw new BusinessException("该需求已关闭或已匹配，无法响应");
        }
        
        // 检查是否已响应过（基于唯一约束）
        long count = count(new LambdaQueryWrapper<DockingRecord>()
                .eq(DockingRecord::getDemandId, request.getDemandId())
                .eq(DockingRecord::getFarmerId, farmerId));
        if (count > 0) {
            throw new BusinessException("您已响应过该需求，请勿重复提交");
        }
        
        // 创建对接记录
        DockingRecord docking = new DockingRecord();
        BeanUtils.copyProperties(request, docking);
        docking.setFarmerId(farmerId);
        docking.setStatus(DockingStatus.PENDING);
        
        save(docking);
        return docking.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleDocking(Long dockingId, Long purchaserId, DockingFeedbackRequest request) {
        DockingRecord docking = getById(dockingId);
        if (docking == null) {
            throw new BusinessException("对接记录不存在");
        }
        
        // 验证采购方权限
        PurchaseDemand demand = purchaseDemandService.getById(docking.getDemandId());
        if (!demand.getPurchaserId().equals(purchaserId)) {
            throw new BusinessException("无权处理该对接记录");
        }
        
        if (docking.getStatus() == DockingStatus.AGREED || docking.getStatus() == DockingStatus.REJECTED) {
            throw new BusinessException("该对接记录已处理，无法重复操作");
        }
        
        // 更新对接状态
        docking.setStatus(request.getStatus());
        docking.setPurchaserRemark(request.getPurchaserRemark());
        updateById(docking);
        
        // 如果达成协议，更新需求状态为已匹配
        if (request.getStatus() == DockingStatus.AGREED) {
            demand.setStatus(DemandStatus.MATCHED);
            purchaseDemandService.updateById(demand);
        }
    }
    
    @Override
    public IPage<DockingRecord> listByDemand(Long demandId, int pageNum, int pageSize) {
        Page<DockingRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DockingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DockingRecord::getDemandId, demandId);
        wrapper.orderByDesc(DockingRecord::getCreateTime);
        
        return page(page, wrapper);
    }
    
    @Override
    public IPage<DockingRecord> listMyDockings(Long userId, String role, int pageNum, int pageSize) {
        Page<DockingRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DockingRecord> wrapper = new LambdaQueryWrapper<>();
        
        if ("farmer".equalsIgnoreCase(role)) {
            // ✅ 修复：获取农户ID后再查询
            FarmerInfo farmer = farmerInfoService.getByUserId(userId);
            if (farmer == null) {
                throw new BusinessException("农户信息不存在");
            }
            wrapper.eq(DockingRecord::getFarmerId, farmer.getId());
        } else if ("purchaser".equalsIgnoreCase(role)) {
            // ✅ 修复SQL注入：先获取采购方信息，再查询需求列表，最后查对接
            PurchaserInfo purchaser = purchaserInfoService.getByUserId(userId);
            if (purchaser != null) {
                List<PurchaseDemand> demands = purchaseDemandService.list(
                        new LambdaQueryWrapper<PurchaseDemand>()
                                .eq(PurchaseDemand::getPurchaserId, purchaser.getId())
                                .select(PurchaseDemand::getId)
                );
                if (!demands.isEmpty()) {
                    List<Long> demandIds = demands.stream()
                            .map(PurchaseDemand::getId)
                            .collect(java.util.stream.Collectors.toList());
                    wrapper.in(DockingRecord::getDemandId, demandIds);
                } else {
                    return page(page, new LambdaQueryWrapper<DockingRecord>().eq(DockingRecord::getId, -1L));
                }
            } else {
                return page(page, new LambdaQueryWrapper<DockingRecord>().eq(DockingRecord::getId, -1L));
            }
        } else {
            // 其他角色返回空
            return page(page, new LambdaQueryWrapper<DockingRecord>().eq(DockingRecord::getId, -1L));
        }
        
        wrapper.orderByDesc(DockingRecord::getCreateTime);
        return page(page, wrapper);
    }
}
