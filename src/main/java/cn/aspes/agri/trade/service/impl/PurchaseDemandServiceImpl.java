package cn.aspes.agri.trade.service.impl;

import cn.aspes.agri.trade.dto.PurchaseDemandRequest;
import cn.aspes.agri.trade.entity.PurchaseDemand;
import cn.aspes.agri.trade.entity.PurchaserInfo;
import cn.aspes.agri.trade.enums.DemandStatus;
import cn.aspes.agri.trade.exception.BusinessException;
import cn.aspes.agri.trade.mapper.PurchaseDemandMapper;
import cn.aspes.agri.trade.service.PurchaseDemandService;
import cn.aspes.agri.trade.service.PurchaserInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;

/**
 * 采购需求服务实现
 */
@Service
public class PurchaseDemandServiceImpl extends ServiceImpl<PurchaseDemandMapper, PurchaseDemand> implements PurchaseDemandService {
    
    @Resource
    private PurchaserInfoService purchaserInfoService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishDemand(Long purchaserId, PurchaseDemandRequest request) {
        // 检查采购方是否存在
        PurchaserInfo purchaserInfo = purchaserInfoService.getById(purchaserId);
        if (purchaserInfo == null) {
            throw new BusinessException("采购方信息不存在");
        }
        
        // 创建采购需求
        PurchaseDemand demand = new PurchaseDemand();
        BeanUtils.copyProperties(request, demand);
        demand.setPurchaserId(purchaserId);
        demand.setStatus(DemandStatus.PENDING);
        
        save(demand);
        return demand.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDemand(Long demandId, Long purchaserId, PurchaseDemandRequest request) {
        PurchaseDemand demand = getById(demandId);
        if (demand == null) {
            throw new BusinessException("采购需求不存在");
        }
        
        if (!demand.getPurchaserId().equals(purchaserId)) {
            throw new BusinessException("无权修改该需求");
        }
        
        if (demand.getStatus() != DemandStatus.PENDING) {
            throw new BusinessException("只能修改待匹配状态的需求");
        }
        
        // 更新需求信息
        BeanUtils.copyProperties(request, demand);
        updateById(demand);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeDemand(Long demandId, Long purchaserId) {
        PurchaseDemand demand = getById(demandId);
        if (demand == null) {
            throw new BusinessException("采购需求不存在");
        }
        
        if (!demand.getPurchaserId().equals(purchaserId)) {
            throw new BusinessException("无权关闭该需求");
        }
        
        demand.setStatus(DemandStatus.CLOSED);
        updateById(demand);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void openDemand(Long demandId, Long purchaserId) {
        PurchaseDemand demand = getById(demandId);
        if (demand == null) {
            throw new BusinessException("采购需求不存在");
        }
        
        if (!demand.getPurchaserId().equals(purchaserId)) {
            throw new BusinessException("无权开启该需求");
        }
        
        if (demand.getStatus() != DemandStatus.CLOSED) {
            throw new BusinessException("只能开启已关闭状态的需求");
        }
        
        demand.setStatus(DemandStatus.PENDING);
        updateById(demand);
    }
    
    @Override
    public IPage<PurchaseDemand> listDemands(int pageNum, int pageSize, Long categoryId, String status) {
        Page<PurchaseDemand> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PurchaseDemand> wrapper = new LambdaQueryWrapper<>();
        
        if (categoryId != null) {
            wrapper.eq(PurchaseDemand::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(PurchaseDemand::getStatus, DemandStatus.valueOf(status.toUpperCase()));
        }
        
        wrapper.orderByDesc(PurchaseDemand::getCreateTime);
        
        return page(page, wrapper);
    }
    
    @Override
    public IPage<PurchaseDemand> listMyDemands(Long purchaserId, int pageNum, int pageSize) {
        Page<PurchaseDemand> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PurchaseDemand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseDemand::getPurchaserId, purchaserId);
        wrapper.orderByDesc(PurchaseDemand::getCreateTime);
        
        return page(page, wrapper);
    }
    
    @Override
    public IPage<PurchaseDemand> searchDemandsByProductName(String keyword, int pageNum, int pageSize) {
        Page<PurchaseDemand> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PurchaseDemand> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(PurchaseDemand::getProductName, keyword);
        }
        
        wrapper.orderByDesc(PurchaseDemand::getCreateTime);
        return page(page, wrapper);
    }
}