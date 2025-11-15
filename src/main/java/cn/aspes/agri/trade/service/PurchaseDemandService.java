package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.dto.PurchaseDemandRequest;
import cn.aspes.agri.trade.entity.PurchaseDemand;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 采购需求服务
 */
public interface PurchaseDemandService extends IService<PurchaseDemand> {
    
    /**
     * 发布采购需求
     */
    Long publishDemand(Long purchaserId, PurchaseDemandRequest request);
    
    /**
     * 更新采购需求
     */
    void updateDemand(Long demandId, Long purchaserId, PurchaseDemandRequest request);
    
    /**
     * 关闭需求
     */
    void closeDemand(Long demandId, Long purchaserId);
    
    /**
     * 开启需求
     */
    void openDemand(Long demandId, Long purchaserId);
    
    /**
     * 分页查询需求列表（公开）
     */
    IPage<PurchaseDemand> listDemands(int pageNum, int pageSize, Long categoryId, String status);
    
    /**
     * 查询我的需求列表
     */
    IPage<PurchaseDemand> listMyDemands(Long purchaserId, int pageNum, int pageSize);
}