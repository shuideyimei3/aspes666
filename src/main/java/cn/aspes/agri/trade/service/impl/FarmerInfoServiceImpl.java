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
import cn.aspes.agri.trade.service.FileUploadService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * 农户信息服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FarmerInfoServiceImpl extends ServiceImpl<FarmerInfoMapper, FarmerInfo> implements FarmerInfoService {
    
    private final UserMapper userMapper;
    private final FileUploadService fileUploadService;
    
    @Override
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
        
        // 在事务外上传身份证
        MultipartFile idCardFrontFile = request.getIdCardFrontFile();
        String idCardFrontUrl = null;
        if (idCardFrontFile != null && !idCardFrontFile.isEmpty()) {
            try {
                idCardFrontUrl = fileUploadService.uploadIdCardFront(idCardFrontFile);
            } catch (Exception e) {
                log.warn("身份证正面照上传失败，用户={}, 错误={}", userId, e.getMessage());
                throw new BusinessException("身份证正面照上传失败：" + e.getMessage());
            }
        }
        
        MultipartFile idCardBackFile = request.getIdCardBackFile();
        String idCardBackUrl = null;
        if (idCardBackFile != null && !idCardBackFile.isEmpty()) {
            try {
                idCardBackUrl = fileUploadService.uploadIdCardBack(idCardBackFile);
            } catch (Exception e) {
                log.warn("身份证反面照上传失败，用户={}, 错误={}", userId, e.getMessage());
                // 删除已成功上传的正面照
                if (idCardFrontUrl != null) {
                    try {
                        fileUploadService.deleteFile(idCardFrontUrl);
                    } catch (Exception deleteError) {
                        log.error("删除身份证正面照失败", deleteError);
                    }
                }
                throw new BusinessException("身份证反面照上传失败：" + e.getMessage());
            }
        }
        
        // 在事务中保存农户信息
        submitFarmerInfoInternal(userId, request, idCardFrontUrl, idCardBackUrl);
    }
    
    /**
     * 内部方法：在事务中提交农户信息
     * 如果事务回滚，会自动删除已上传的身份证
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitFarmerInfoInternal(Long userId, FarmerInfoRequest request, String idCardFrontUrl, String idCardBackUrl) {
        // 注册事务回滚时的文件删除回调
        if (idCardFrontUrl != null && TransactionSynchronizationManager.isActualTransactionActive()) {
            registerFilesDeleteOnRollback(idCardFrontUrl, idCardBackUrl);
        }
        
        // 创建农户信息（包含认证信息）
        FarmerInfo farmerInfo = new FarmerInfo();
        BeanUtils.copyProperties(request, farmerInfo);
        farmerInfo.setUserId(userId);
        farmerInfo.setAuditStatus(AuditStatus.PENDING);
        
        // 设置上传后的URL
        if (idCardFrontUrl != null) {
            farmerInfo.setIdCardFrontUrl(idCardFrontUrl);
        }
        if (idCardBackUrl != null) {
            farmerInfo.setIdCardBackUrl(idCardBackUrl);
        }
        
        save(farmerInfo);
    }
    
    /**
     * 注册事务回滚时的文件删除回调
     */
    private void registerFilesDeleteOnRollback(String idCardFrontUrl, String idCardBackUrl) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                // status = TransactionSynchronization.STATUS_ROLLED_BACK 表示事务已回滚
                if (status == STATUS_ROLLED_BACK) {
                    if (idCardFrontUrl != null) {
                        try {
                            fileUploadService.deleteFile(idCardFrontUrl);
                            log.info("事务回滚：已自动删除身份证正面照，URL={}", idCardFrontUrl);
                        } catch (Exception e) {
                            log.error("事务回滚时删除文件失败，URL={}", idCardFrontUrl, e);
                        }
                    }
                    if (idCardBackUrl != null) {
                        try {
                            fileUploadService.deleteFile(idCardBackUrl);
                            log.info("事务回滚：已自动删除身份证反面照，URL={}", idCardBackUrl);
                        } catch (Exception e) {
                            log.error("事务回滚时删除文件失败，URL={}", idCardBackUrl, e);
                        }
                    }
                }
            }
        });
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
        
        // 保存旧的图片URL，用于后续删除
        String oldIdCardFrontUrl = farmerInfo.getIdCardFrontUrl();
        String oldIdCardBackUrl = farmerInfo.getIdCardBackUrl();
        
        // 更新信息
        BeanUtils.copyProperties(request, farmerInfo);
        farmerInfo.setId(farmerId);
        updateById(farmerInfo);
        
        // 删除旧的身份证图片（如果URL发生变化）
        deleteOldImageIfChanged(oldIdCardFrontUrl, farmerInfo.getIdCardFrontUrl());
        deleteOldImageIfChanged(oldIdCardBackUrl, farmerInfo.getIdCardBackUrl());
    }
    
    /**
     * 删除旧图片（如果URL发生变化）
     */
    private void deleteOldImageIfChanged(String oldUrl, String newUrl) {
        if (oldUrl != null && !oldUrl.isEmpty() && !oldUrl.equals(newUrl)) {
            try {
                fileUploadService.deleteFile(oldUrl);
                log.info("已删除旧图片文件，URL={}", oldUrl);
            } catch (Exception e) {
                log.error("删除旧图片文件失败，URL={}", oldUrl, e);
            }
        }
    }
    
    @Override
    public Page<FarmerInfo> searchFarmersByName(String keyword, Integer current, Integer size) {
        Page<FarmerInfo> page = new Page<>(current, size);
        LambdaQueryWrapper<FarmerInfo> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(FarmerInfo::getFarmName, keyword);
        }
        
        wrapper.orderByDesc(FarmerInfo::getCreateTime);
        return page(page, wrapper);
    }
}