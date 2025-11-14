package cn.aspes.agri.trade.service.impl;

import cn.aspes.agri.trade.dto.UserCertificationRequest;
import cn.aspes.agri.trade.entity.User;
import cn.aspes.agri.trade.entity.UserCertificationApply;
import cn.aspes.agri.trade.exception.BusinessException;
import cn.aspes.agri.trade.mapper.UserCertificationApplyMapper;
import cn.aspes.agri.trade.service.UserCertificationService;
import cn.aspes.agri.trade.service.UserService;
import cn.aspes.agri.trade.util.SnowflakeIdGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCertificationServiceImpl extends ServiceImpl<UserCertificationApplyMapper, UserCertificationApply> implements UserCertificationService {
    
    private final UserService userService;
    private final SnowflakeIdGenerator idGenerator;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitCertification(Long userId, UserCertificationRequest request) {
        // 检查用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 检查是否已提交过该类型的认证申请
        UserCertificationApply existing = getOne(new LambdaQueryWrapper<UserCertificationApply>()
                .eq(UserCertificationApply::getUserId, userId)
                .eq(UserCertificationApply::getApplyType, request.getApplyType()));
        
        if (existing != null && "pending".equals(existing.getStatus())) {
            throw new BusinessException("您已有一份待审核的认证申请，请耐心等待");
        }
        
        // 创建认证申请
        UserCertificationApply apply = new UserCertificationApply();
        apply.setId(idGenerator.nextId());
        apply.setUserId(userId);
        apply.setApplyType(request.getApplyType());
        apply.setIdNumber(request.getIdNumber());
        apply.setIdCardFrontUrl(request.getIdCardFrontUrl());
        apply.setIdCardBackUrl(request.getIdCardBackUrl());
        apply.setBusinessLicenseUrl(request.getBusinessLicenseUrl());
        apply.setLegalRepresentative(request.getLegalRepresentative());
        apply.setApplyReason(request.getApplyReason());
        apply.setStatus("pending");
        
        save(apply);
        log.info("用户认证申请已提交：用户={}, 类型={}", userId, request.getApplyType());
        
        return apply.getId();
    }
    
    @Override
    public UserCertificationApply getUserCertification(Long userId, String applyType) {
        return getOne(new LambdaQueryWrapper<UserCertificationApply>()
                .eq(UserCertificationApply::getUserId, userId)
                .eq(UserCertificationApply::getApplyType, applyType)
                .orderByDesc(UserCertificationApply::getCreateTime)
                .last("LIMIT 1"));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveCertification(Long applyId, String adminRemark) {
        UserCertificationApply apply = getById(applyId);
        if (apply == null) {
            throw new BusinessException("认证申请不存在");
        }
        
        if (!"pending".equals(apply.getStatus())) {
            throw new BusinessException("该申请已处理，无法重复操作");
        }
        
        // 更新申请状态
        apply.setStatus("approved");
        apply.setAdminRemark(adminRemark);
        apply.setApprovedTime(LocalDateTime.now());
        updateById(apply);
        
        // 更新用户认证状态
        User user = userService.getById(apply.getUserId());
        if (user != null) {
            user.setIsCertified(1);
            userService.updateById(user);
            log.info("用户认证已批准：用户={}, 类型={}", apply.getUserId(), apply.getApplyType());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectCertification(Long applyId, String rejectReason) {
        UserCertificationApply apply = getById(applyId);
        if (apply == null) {
            throw new BusinessException("认证申请不存在");
        }
        
        if (!"pending".equals(apply.getStatus())) {
            throw new BusinessException("该申请已处理，无法重复操作");
        }
        
        // 更新申请状态
        apply.setStatus("rejected");
        apply.setAdminRemark(rejectReason);
        updateById(apply);
        
        log.info("用户认证已拒绝：用户={}, 类型={}, 原因={}", apply.getUserId(), apply.getApplyType(), rejectReason);
    }
    
    @Override
    public Page<UserCertificationApply> pagePendingApplications(Integer current, Integer size, String applyType) {
        Page<UserCertificationApply> page = new Page<>(current, size);
        LambdaQueryWrapper<UserCertificationApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCertificationApply::getStatus, "pending");
        
        if (applyType != null && !applyType.isEmpty()) {
            wrapper.eq(UserCertificationApply::getApplyType, applyType);
        }
        
        wrapper.orderByDesc(UserCertificationApply::getCreateTime);
        return page(page, wrapper);
    }
    
    @Override
    public Page<UserCertificationApply> pageApplications(Integer current, Integer size, String status) {
        Page<UserCertificationApply> page = new Page<>(current, size);
        LambdaQueryWrapper<UserCertificationApply> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(UserCertificationApply::getStatus, status);
        }
        
        wrapper.orderByDesc(UserCertificationApply::getCreateTime);
        return page(page, wrapper);
    }
}
