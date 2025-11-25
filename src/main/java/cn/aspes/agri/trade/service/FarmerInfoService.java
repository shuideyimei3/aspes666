package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.dto.*;
import cn.aspes.agri.trade.entity.FarmerInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 农户信息服务
 */
public interface FarmerInfoService extends IService<FarmerInfo> {
    
    /**
     * 提交农户信息（待审核）
     */
    void submitFarmerInfo(Long userId, FarmerInfoRequest request);
    
    /**
     * 审核农户信息
     */
    void auditFarmerInfo(Long farmerId, AuditRequest request);
    
    /**
     * 根据用户ID查询农户信息
     */
    FarmerInfo getByUserId(Long userId);
    
    /**
     * 分页查询农户列表（管理员）
     */
    Page<FarmerInfo> pageFarmers(Integer current, Integer size, String auditStatus);
    
    /**
     * 修改农户信息
     */
    void updateFarmerInfo(Long farmerId, Long userId, FarmerInfoRequest request);
    
    /**
     * 根据农户名称关键字查询农户列表（分页）
     */
    Page<FarmerInfo> searchFarmersByName(String keyword, Integer current, Integer size);
}