package cn.aspes.agri.trade.service.impl;

import cn.aspes.agri.trade.dto.AuditRequest;
import cn.aspes.agri.trade.dto.FarmerInfoRequest;
import cn.aspes.agri.trade.entity.FarmerInfo;
import cn.aspes.agri.trade.entity.User;
import cn.aspes.agri.trade.enums.AuditStatus;
import cn.aspes.agri.trade.enums.UserRole;
import cn.aspes.agri.trade.exception.BusinessException;
import cn.aspes.agri.trade.mapper.FarmerInfoMapper;
import cn.aspes.agri.trade.mapper.UserMapper;
import cn.aspes.agri.trade.service.FarmerInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

/**
 * 农户信息服务实现
 */
@Service
public class FarmerInfoServiceImpl extends ServiceImpl<FarmerInfoMapper, FarmerInfo> implements FarmerInfoService {
    
    @Resource
    private UserMapper userMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitFarmerInfo(Long userId, FarmerInfoRequest request) {
        // 检查用户是否存在且角色正确
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getRole() != UserRole.FARMER) {
            throw new BusinessException("只有农户角色才能提交农户信息");
        }
        
        // 检查是否已提交
        FarmerInfo existing = getByUserId(userId);
        if (existing != null) {
            throw new BusinessException("农户信息已存在，请勿重复提交");
        }
        
        // 创建农户信息（包含认证信息）
        FarmerInfo farmerInfo = new FarmerInfo();
        BeanUtils.copyProperties(request, farmerInfo);
        farmerInfo.setUserId(userId);
        farmerInfo.setAuditStatus(AuditStatus.PENDING);
        
        save(farmerInfo);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditFarmerInfo(Long farmerId, AuditRequest request) {
        FarmerInfo farmerInfo = getById(farmerId);
        if (farmerInfo == null) {
            throw new BusinessException("农户信息不存在");
        }
        
        if (farmerInfo.getAuditStatus() != AuditStatus.PENDING) {
            throw new BusinessException("该农户信息已审核，无法重复审核");
        }
        
        // 更新审核状态
        farmerInfo.setAuditStatus(request.getAuditStatus());
        farmerInfo.setAuditRemark(request.getAuditRemark());
        
        // 如果审核通过，设置批准时间和更新用户认证状态
        if (request.getAuditStatus() == AuditStatus.APPROVED) {
            farmerInfo.setApprovedTime(LocalDateTime.now());
            User user = userMapper.selectById(farmerInfo.getUserId());
            user.setIsCertified(1);
            userMapper.updateById(user);
        }
        
        updateById(farmerInfo);
    }
    
    @Override
    public FarmerInfo getByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<FarmerInfo>()
                .eq(FarmerInfo::getUserId, userId));
    }
    
    @Override
    public Page<FarmerInfo> pageFarmers(Integer current, Integer size, String auditStatus) {
        Page<FarmerInfo> page = new Page<>(current, size);
        LambdaQueryWrapper<FarmerInfo> wrapper = new LambdaQueryWrapper<>();
        
        if (auditStatus != null && !auditStatus.isEmpty()) {
            wrapper.eq(FarmerInfo::getAuditStatus, AuditStatus.valueOf(auditStatus.toUpperCase()));
        }
        
        wrapper.orderByDesc(FarmerInfo::getCreateTime);
        return page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFarmerInfo(Long farmerId, Long userId, FarmerInfoRequest request) {
        FarmerInfo farmerInfo = getById(farmerId);
        if (farmerInfo == null) {
            throw new BusinessException("农户信息不存在");
        }
        
        // 验证权限
        if (!farmerInfo.getUserId().equals(userId)) {
            throw new BusinessException("无权修改该农户信息");
        }
        
        // 更新信息
        BeanUtils.copyProperties(request, farmerInfo);
        farmerInfo.setId(farmerId);
        updateById(farmerInfo);
    }
}