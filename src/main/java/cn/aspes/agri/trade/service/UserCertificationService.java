package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.dto.UserCertificationRequest;
import cn.aspes.agri.trade.entity.UserCertificationApply;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户认证服务接口
 */
public interface UserCertificationService extends IService<UserCertificationApply> {
    
    /**
     * 提交认证申请
     */
    Long submitCertification(Long userId, UserCertificationRequest request);
    
    /**
     * 获取用户的认证申请状态
     */
    UserCertificationApply getUserCertification(Long userId, String applyType);
    
    /**
     * 管理员审核认证申请（批准）
     */
    void approveCertification(Long applyId, String adminRemark);
    
    /**
     * 管理员审核认证申请（拒绝）
     */
    void rejectCertification(Long applyId, String rejectReason);
    
    /**
     * 分页查询待审核的认证申请
     */
    Page<UserCertificationApply> pagePendingApplications(Integer current, Integer size, String applyType);
    
    /**
     * 分页查询所有认证申请
     */
    Page<UserCertificationApply> pageApplications(Integer current, Integer size, String status);
}
