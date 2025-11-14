package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.vo.StatisticsVO;

/**
 * 数据统计分析服务接口
 */
public interface StatisticsService {
    
    /**
     * 获取用户订单统计
     */
    StatisticsVO.UserOrderStats getUserOrderStats(Long userId, String role);
    
    /**
     * 获取产品销售统计
     */
    StatisticsVO.ProductSalesStats getProductSalesStats(Long productId);
    
    /**
     * 获取平台数据统计（管理员）
     */
    StatisticsVO.PlatformStats getPlatformStats();
    
    /**
     * 获取采购方采购统计
     */
    StatisticsVO.PurchaserStats getPurchaserStats(Long purchaserId);
    
    /**
     * 获取农户销售统计
     */
    StatisticsVO.FarmerStats getFarmerStats(Long farmerId);
}
