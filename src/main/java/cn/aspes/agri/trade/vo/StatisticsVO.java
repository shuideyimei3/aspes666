package cn.aspes.agri.trade.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 数据统计VO类
 */
public class StatisticsVO {
    
    /**
     * 用户订单统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserOrderStats {
        private Long completedOrders;           // 已完成订单数
        private Long totalOrders;               // 总订单数
        private BigDecimal totalAmount;         // 总交易额
        private BigDecimal averageAmount;       // 平均订单金额
        private Double completionRate;          // 完成率
    }
    
    /**
     * 产品销售统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSalesStats {
        private String productName;             // 产品名称
        private Integer salesCount;             // 销售数量
        private BigDecimal salesAmount;         // 销售总额
        private Integer reviewCount;            // 评价数
        private Double averageRating;           // 平均评分
    }
    
    /**
     * 平台统计数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlatformStats {
        private Long totalUsers;                // 总用户数
        private Long totalFarmers;              // 农户数
        private Long totalPurchasers;           // 采购方数
        private Long totalProducts;             // 商品总数
        private Long totalOrders;               // 订单总数
        private BigDecimal totalTransactionAmount;  // 交易总额
        private Long activeUsersToday;          // 今日活跃用户数
    }
    
    /**
     * 采购方统计数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaserStats {
        private Long demandCount;               // 发布的需求数
        private Long ordersCount;               // 订单数
        private BigDecimal totalPurchase;       // 总采购额
        private Double averageOrderAmount;      // 平均订单金额
    }
    
    /**
     * 农户统计数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FarmerStats {
        private Integer productCount;           // 发布的产品数
        private Long salesOrders;               // 已销售订单数
        private BigDecimal totalSales;          // 总销售额
        private Double averageRating;           // 平均评分
    }
}