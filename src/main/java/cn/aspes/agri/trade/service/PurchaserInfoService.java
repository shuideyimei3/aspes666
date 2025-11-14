package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.dto.AuditRequest;
import cn.aspes.agri.trade.dto.PurchaserInfoRequest;
import cn.aspes.agri.trade.entity.PurchaserInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 采购方信息服务
 */
public interface PurchaserInfoService extends IService<PurchaserInfo> {
    
    /**
     * 提交采购方信息（待审核）
     */
    void submitPurchaserInfo(Long userId, PurchaserInfoRequest request);
    
    /**
     * 审核采购方信息
     */
    void auditPurchaserInfo(Long purchaserId, AuditRequest request);
    
    /**
     * 根据用户ID查询采购方信息
     */
    PurchaserInfo getByUserId(Long userId);
    
    /**
     * 分页查询采购方
     */
    Page<PurchaserInfo> pagePurchasers(Integer current, Integer size, String auditStatus);
    
    /**
     * 修改采购方信息
     */
    void updatePurchaserInfo(Long purchaserId, Long userId, PurchaserInfoRequest request);
}
