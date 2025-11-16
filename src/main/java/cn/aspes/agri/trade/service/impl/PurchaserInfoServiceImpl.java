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
 * 采购方信息服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaserInfoServiceImpl extends ServiceImpl<PurchaserInfoMapper, PurchaserInfo> implements PurchaserInfoService {
    
    private final UserMapper userMapper;
    private final FileUploadService fileUploadService;
    
    @Override
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
        
        // 在事务外上传营业执照
        MultipartFile businessLicenseFile = request.getBusinessLicenseFile();
        String businessLicenseUrl = null;
        if (businessLicenseFile != null && !businessLicenseFile.isEmpty()) {
            try {
                businessLicenseUrl = fileUploadService.uploadBusinessLicense(businessLicenseFile);
            } catch (Exception e) {
                log.warn("营业执照上传失败，用户={}, 错误={}", userId, e.getMessage());
                throw new BusinessException("营业执照上传失败：" + e.getMessage());
            }
        }
        
        // 在事务中保存采购方信息
        submitPurchaserInfoInternal(userId, request, businessLicenseUrl);
    }
    
    /**
     * 内部方法：在事务中提交采购方信息
     * 如果事务回滚，会自动删除已上传的营业执照
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitPurchaserInfoInternal(Long userId, PurchaserInfoRequest request, String businessLicenseUrl) {
        // 注册事务回滚时的文件删除回调
        if (businessLicenseUrl != null && TransactionSynchronizationManager.isActualTransactionActive()) {
            registerFileDeleteOnRollback(businessLicenseUrl);
        }
        
        // 创建采购方信息（包含认证信息）
        PurchaserInfo purchaserInfo = new PurchaserInfo();
        BeanUtils.copyProperties(request, purchaserInfo);
        purchaserInfo.setUserId(userId);
        purchaserInfo.setAuditStatus(AuditStatus.PENDING);
        
        // 设置上传后的URL
        if (businessLicenseUrl != null) {
            purchaserInfo.setBusinessLicenseUrl(businessLicenseUrl);
        }
        
        save(purchaserInfo);
    }
    
    /**
     * 注册事务回滚时的文件删除回调
     */
    private void registerFileDeleteOnRollback(String fileUrl) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                // status = TransactionSynchronization.STATUS_ROLLED_BACK 表示事务已回滚
                if (status == STATUS_ROLLED_BACK) {
                    try {
                        fileUploadService.deleteFile(fileUrl);
                        log.info("事务回滚：已自动删除上传的营业执照，URL={}", fileUrl);
                    } catch (Exception e) {
                        log.error("事务回滚时删除文件失败，URL={}", fileUrl, e);
                    }
                }
            }
        });
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
        
        // 如果审核通过，设置批准时间和更新用户认证状态
        if (request.getAuditStatus() == AuditStatus.APPROVED) {
            purchaserInfo.setApprovedTime(LocalDateTime.now());
            User user = userMapper.selectById(purchaserInfo.getUserId());
            user.setIsCertified(1);
            userMapper.updateById(user);
        }
        
        updateById(purchaserInfo);
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
    public PurchaserInfo getByName(String name) {
        return getOne(new LambdaQueryWrapper<PurchaserInfo>()
                .eq(PurchaserInfo::getCompanyName, name));
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
        
        // 保存旧的营业执照URL，用于后续删除
        String oldBusinessLicenseUrl = purchaserInfo.getBusinessLicenseUrl();
        
        // 更新信息
        BeanUtils.copyProperties(request, purchaserInfo);
        purchaserInfo.setId(purchaserId);
        updateById(purchaserInfo);
        
        // 删除旧的营业执照（如果URL发生变化）
        deleteOldImageIfChanged(oldBusinessLicenseUrl, purchaserInfo.getBusinessLicenseUrl());
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
}