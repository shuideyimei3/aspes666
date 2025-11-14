package cn.aspes.agri.trade.service.impl;

import cn.aspes.agri.trade.dto.AuditRequest;
import cn.aspes.agri.trade.dto.PurchaserInfoRequest;
import cn.aspes.agri.trade.entity.PurchaserInfo;
import cn.aspes.agri.trade.entity.User;
import cn.aspes.agri.trade.enums.AuditStatus;
import cn.aspes.agri.trade.enums.UserRole;
import cn.aspes.agri.trade.exception.BusinessException;
import cn.aspes.agri.trade.mapper.PurchaserInfoMapper;
import cn.aspes.agri.trade.mapper.UserMapper;
import cn.aspes.agri.trade.service.PurchaserInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

/**
 * 采购方信息服务实现
 */
@Service
public class PurchaserInfoServiceImpl extends ServiceImpl<PurchaserInfoMapper, PurchaserInfo> implements PurchaserInfoService {
    
    @Resource
    private UserMapper userMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitPurchaserInfo(Long userId, PurchaserInfoRequest request) {
        // 检查用户是否存在且角色正确
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getRole() != UserRole.PURCHASER) {
            throw new BusinessException("只有采购方角色才能提交采购方信息");
        }
        
        // 检查是否已提交
        PurchaserInfo existing = getByUserId(userId);
        if (existing != null) {
            throw new BusinessException("采购方信息已存在，请勿重复提交");
        }
        
        // 创建采购方信息
        PurchaserInfo purchaserInfo = new PurchaserInfo();
        BeanUtils.copyProperties(request, purchaserInfo);
        purchaserInfo.setUserId(userId);
        purchaserInfo.setAuditStatus(AuditStatus.PENDING);
        
        save(purchaserInfo);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditPurchaserInfo(Long purchaserId, AuditRequest request) {
        PurchaserInfo purchaserInfo = getById(purchaserId);
        if (purchaserInfo == null) {
            throw new BusinessException("采购方信息不存在");
        }
        
        if (purchaserInfo.getAuditStatus() != AuditStatus.PENDING) {
            throw new BusinessException("该采购方信息已审核，无法重复审核");
        }
        
        // 更新审核状态
        purchaserInfo.setAuditStatus(request.getAuditStatus());
        purchaserInfo.setAuditRemark(request.getAuditRemark());
        updateById(purchaserInfo);
        
        // 如果审核通过，更新用户认证状态
        if (request.getAuditStatus() == AuditStatus.APPROVED) {
            User user = userMapper.selectById(purchaserInfo.getUserId());
            user.setIsCertified(1);
            userMapper.updateById(user);
        }
    }
    
    @Override
    public PurchaserInfo getByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<PurchaserInfo>()
                .eq(PurchaserInfo::getUserId, userId));
    }
    
    @Override
    public Page<PurchaserInfo> pagePurchasers(Integer current, Integer size, String auditStatus) {
        Page<PurchaserInfo> page = new Page<>(current, size);
        LambdaQueryWrapper<PurchaserInfo> wrapper = new LambdaQueryWrapper<>();
        
        if (auditStatus != null && !auditStatus.isEmpty()) {
            wrapper.eq(PurchaserInfo::getAuditStatus, AuditStatus.valueOf(auditStatus.toUpperCase()));
        }
        
        wrapper.orderByDesc(PurchaserInfo::getCreateTime);
        return page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaserInfo(Long purchaserId, Long userId, PurchaserInfoRequest request) {
        PurchaserInfo purchaserInfo = getById(purchaserId);
        if (purchaserInfo == null) {
            throw new BusinessException("采购方信息不存在");
        }
        
        // 验证权限
        if (!purchaserInfo.getUserId().equals(userId)) {
            throw new BusinessException("无权修改该采购方信息");
        }
        
        // 更新信息
        BeanUtils.copyProperties(request, purchaserInfo);
        purchaserInfo.setId(purchaserId);
        updateById(purchaserInfo);
    }
}
