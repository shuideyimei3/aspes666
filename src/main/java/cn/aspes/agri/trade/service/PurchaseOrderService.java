package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.entity.PurchaseOrder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 采购订单服务接口
 */
public interface PurchaseOrderService extends IService<PurchaseOrder> {
    
    /**
     * 基于合同创建订单
     */
    PurchaseOrder createOrderFromContract(Long contractId);
    
    /**
     * 农户交货
     */
    void deliverOrder(Long orderId, Integer actualQuantity, String inspectionResult);
    
    /**
     * 订单完成
     */
    void completeOrder(Long orderId);
    
    /**
     * 取消订单
     */
    void cancelOrder(Long orderId, String reason);
    
    /**
     * 分页查询订单
     */
    Page<PurchaseOrder> pageOrders(Integer current, Integer size, String status);
    
    /**
     * 查询我的订单
     */
    Page<PurchaseOrder> listMyOrders(Long userId, String role, Integer current, Integer size);
    
    /**
     * 订单详情
     */
    PurchaseOrder getOrderDetail(Long orderId, Long currentUserId, String role);
}