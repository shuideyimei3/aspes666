package cn.aspes.agri.trade.service.impl;

import cn.aspes.agri.trade.entity.*;
import cn.aspes.agri.trade.enums.OrderStatus;
import cn.aspes.agri.trade.enums.UserRole;
import cn.aspes.agri.trade.mapper.*;
import cn.aspes.agri.trade.service.StatisticsService;
import cn.aspes.agri.trade.service.FarmerInfoService;
import cn.aspes.agri.trade.service.PurchaserInfoService;
import cn.aspes.agri.trade.vo.StatisticsVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 数据统计分析服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    
    private final UserMapper userMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final FarmerProductMapper farmerProductMapper;
    private final PurchaseDemandMapper purchaseDemandMapper;
    private final CooperationReviewMapper cooperationReviewMapper;
    private final PurchaseContractMapper purchaseContractMapper;
    private final FarmerInfoMapper farmerInfoMapper;
    private final PurchaserInfoMapper purchaserInfoMapper;
    private final OriginAreaMapper originAreaMapper;
    private final FarmerInfoService farmerInfoService;
    private final PurchaserInfoService purchaserInfoService;
    
    // 市级农户活跃事件滑动窗口，按城市聚合（内存实现）
    private final java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.ConcurrentLinkedQueue<Long>> cityEvents = new java.util.concurrent.ConcurrentHashMap<>();
    
    @Override
    public StatisticsVO.UserOrderStats getUserOrderStats(Long userId, String role) {
        StatisticsVO.UserOrderStats stats = new StatisticsVO.UserOrderStats();
        
        // 查询用户的所有订单
        List<PurchaseOrder> orders;
        if ("farmer".equalsIgnoreCase(role)) {
            // 农户：通过合同查询其作为卖方的订单
            // ✅ 修复：先获取farmer对象，再查询合同
            FarmerInfo farmer = farmerInfoService.getByUserId(userId);
            if (farmer == null) {
                return new StatisticsVO.UserOrderStats(0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO, 0.0);
            }
            List<PurchaseContract> contracts = purchaseContractMapper.selectList(
                    new LambdaQueryWrapper<PurchaseContract>()
                            .eq(PurchaseContract::getFarmerId, farmer.getId()));
            List<Long> contractIds = contracts.stream().map(PurchaseContract::getId).toList();
            if (contractIds.isEmpty()) {
                return new StatisticsVO.UserOrderStats(0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO, 0.0);
            }
            orders = purchaseOrderMapper.selectList(
                    new LambdaQueryWrapper<PurchaseOrder>()
                            .in(PurchaseOrder::getContractId, contractIds));
        } else {
            // 采购方：通过合同查询其作为买方的订单
            // ✅ 修复：先获取purchaser对象，再查询合同
            PurchaserInfo purchaser = purchaserInfoService.getByUserId(userId);
            if (purchaser == null) {
                return new StatisticsVO.UserOrderStats(0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO, 0.0);
            }
            List<PurchaseContract> contracts = purchaseContractMapper.selectList(
                    new LambdaQueryWrapper<PurchaseContract>()
                            .eq(PurchaseContract::getPurchaserId, purchaser.getId()));
            List<Long> contractIds = contracts.stream().map(PurchaseContract::getId).toList();
            if (contractIds.isEmpty()) {
                return new StatisticsVO.UserOrderStats(0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO, 0.0);
            }
            orders = purchaseOrderMapper.selectList(
                    new LambdaQueryWrapper<PurchaseOrder>()
                            .in(PurchaseOrder::getContractId, contractIds));
        }
        
        // 统计数据
        long completedOrders = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .count();
        long totalOrders = orders.size();
        BigDecimal totalAmount = orders.stream()
                .filter(o -> o.getActualAmount() != null)
                .map(PurchaseOrder::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageAmount = totalOrders > 0 
                ? totalAmount.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        double completionRate = totalOrders > 0 
                ? (double) completedOrders / totalOrders * 100
                : 0.0;
        
        stats.setCompletedOrders(completedOrders);
        stats.setTotalOrders(totalOrders);
        stats.setTotalAmount(totalAmount);
        stats.setAverageAmount(averageAmount);
        stats.setCompletionRate(completionRate);
        
        return stats;
    }
    
    @Override
    public StatisticsVO.ProductSalesStats getProductSalesStats(Long productId) {
        FarmerProduct product = farmerProductMapper.selectById(productId);
        if (product == null) {
            return new StatisticsVO.ProductSalesStats("", 0, BigDecimal.ZERO, 0, 0.0);
        }
        
        // ✅ 修复：计算销售数据（通过订单中的product_info字段）
        int salesCount = 0;
        BigDecimal salesAmount = BigDecimal.ZERO;
        
        // 查询包含该产品的所有已完成订单
        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrder>()
                        .eq(PurchaseOrder::getStatus, OrderStatus.COMPLETED));
        
        for (PurchaseOrder order : orders) {
            // 解析 product_info JSON 字段
            Map<String, Object> productInfo = order.getProductInfo();
            if (productInfo != null) {
                Object prodIdObj = productInfo.get("productId");
                if (prodIdObj != null) {
                    Long prodId = Long.valueOf(prodIdObj.toString());
                    if (prodId.equals(productId)) {
                        Integer quantity = Integer.valueOf(productInfo.get("quantity").toString());
                        BigDecimal price = new BigDecimal(productInfo.get("price").toString());
                        salesCount += quantity;
                        salesAmount = salesAmount.add(price.multiply(new BigDecimal(quantity)));
                    }
                }
            }
        }
        
        // 查询评价
        List<CooperationReview> reviews = cooperationReviewMapper.selectList(
                new LambdaQueryWrapper<CooperationReview>()
                        .eq(CooperationReview::getTargetId, product.getFarmerId())
                        .eq(CooperationReview::getReviewTo, "farmer"));
        
        int reviewCount = reviews.size();
        double averageRating = reviews.isEmpty() ? 0.0 :
                reviews.stream()
                        .mapToInt(CooperationReview::getRating)
                        .average()
                        .orElse(0.0);
        
        return new StatisticsVO.ProductSalesStats(
                product.getName(),
                salesCount,
                salesAmount,
                reviewCount,
                averageRating
        );
    }
    
    @Override
    public StatisticsVO.PlatformStats getPlatformStats() {
        StatisticsVO.PlatformStats stats = new StatisticsVO.PlatformStats();
        
        // 统计总用户数
        stats.setTotalUsers(userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getIsDelete, 0)));
        
        // 统计农户数
        stats.setTotalFarmers(userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getRole, UserRole.FARMER)
                        .eq(User::getIsDelete, 0)));
        
        // 统计采购方数
        stats.setTotalPurchasers(userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getRole, UserRole.PURCHASER)
                        .eq(User::getIsDelete, 0)));
        
        // 统计商品总数
        stats.setTotalProducts(farmerProductMapper.selectCount(null));
        
        // 统计订单总数
        stats.setTotalOrders(purchaseOrderMapper.selectCount(null));
        
        // 统计交易总额
        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrder>()
                        .eq(PurchaseOrder::getStatus, OrderStatus.COMPLETED));
        BigDecimal totalAmount = orders.stream()
                .filter(o -> o.getActualAmount() != null)
                .map(PurchaseOrder::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalTransactionAmount(totalAmount);
        
        // 统计今日活跃用户（简化：今日创建的订单数）
        LocalDate today = LocalDate.now();
        Long activeToday = purchaseOrderMapper.selectCount(
                new LambdaQueryWrapper<PurchaseOrder>()
                        .ge(PurchaseOrder::getCreateTime, today.atStartOfDay()));
        stats.setActiveUsersToday(activeToday);
        
        return stats;
    }
    
    @Override
    public StatisticsVO.PurchaserStats getPurchaserStats(Long purchaserId) {
        StatisticsVO.PurchaserStats stats = new StatisticsVO.PurchaserStats();
        
        PurchaserInfo purchaser = purchaserInfoMapper.selectById(purchaserId);
        if (purchaser == null) {
            return stats;
        }
        
        // 查询采购方的所有合同
        List<PurchaseContract> contracts = purchaseContractMapper.selectList(
                new LambdaQueryWrapper<PurchaseContract>()
                        .eq(PurchaseContract::getPurchaserId, purchaserId));
        List<Long> contractIds = contracts.stream().map(PurchaseContract::getId).toList();
        
        if (!contractIds.isEmpty()) {
            // 查询所有订单
            List<PurchaseOrder> orders = purchaseOrderMapper.selectList(
                    new LambdaQueryWrapper<PurchaseOrder>()
                            .in(PurchaseOrder::getContractId, contractIds));
            
            // 统计采购需求
            long totalDemands = purchaseDemandMapper.selectCount(
                    new LambdaQueryWrapper<PurchaseDemand>()
                            .eq(PurchaseDemand::getPurchaserId, purchaserId));
            stats.setDemandCount(totalDemands);
            
            // 统计订单
            stats.setOrdersCount((long) orders.size());
            
            // 统计交易金额
            BigDecimal totalAmount = orders.stream()
                    .filter(o -> o.getActualAmount() != null)
                    .map(PurchaseOrder::getActualAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.setTotalPurchase(totalAmount);
            
            // 计算平均订单金额
            double averageAmount = orders.isEmpty() ? 0.0 :
                    totalAmount.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP).doubleValue();
            stats.setAverageOrderAmount(averageAmount);
        }
        
        return stats;
    }
    
    @Override
    public StatisticsVO.FarmerStats getFarmerStats(Long farmerId) {
        StatisticsVO.FarmerStats stats = new StatisticsVO.FarmerStats();
        
        FarmerInfo farmer = farmerInfoMapper.selectById(farmerId);
        if (farmer == null) {
            return stats;
        }
        
        // 统计产品数量
        Long productCountLong = farmerProductMapper.selectCount(
                new LambdaQueryWrapper<FarmerProduct>()
                        .eq(FarmerProduct::getFarmerId, farmerId));
        stats.setProductCount(productCountLong.intValue());
        
        // 查询农户的所有合同
        List<PurchaseContract> contracts = purchaseContractMapper.selectList(
                new LambdaQueryWrapper<PurchaseContract>()
                        .eq(PurchaseContract::getFarmerId, farmerId));
        List<Long> contractIds = contracts.stream().map(PurchaseContract::getId).toList();
        
        if (!contractIds.isEmpty()) {
            // 查询所有订单
            List<PurchaseOrder> orders = purchaseOrderMapper.selectList(
                    new LambdaQueryWrapper<PurchaseOrder>()
                            .in(PurchaseOrder::getContractId, contractIds));
            
            // 统计已完成订单数
            long completedOrders = orders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                    .count();
            stats.setSalesOrders(completedOrders);
            
            // 统计销售额
            BigDecimal totalAmount = orders.stream()
                    .filter(o -> o.getActualAmount() != null)
                    .map(PurchaseOrder::getActualAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.setTotalSales(totalAmount);
            
            // 统计平均评分
            List<CooperationReview> reviews = cooperationReviewMapper.selectList(
                    new LambdaQueryWrapper<CooperationReview>()
                            .eq(CooperationReview::getTargetId, farmerId)
                            .eq(CooperationReview::getReviewTo, "farmer"));
            double averageRating = reviews.isEmpty() ? 0.0 :
                    reviews.stream()
                            .mapToInt(CooperationReview::getRating)
                            .average()
                            .orElse(0.0);
            stats.setAverageRating(averageRating);
        }
        
        return stats;
    }
    
    // 记录农户请求活跃事件（市级）
    @Override
    public void recordFarmerActivity(Long userId) {
        try {
            FarmerInfo farmer = farmerInfoService.getByUserId(userId);
            if (farmer == null || farmer.getOriginAreaId() == null) {
                return;
            }
            OriginArea area = originAreaMapper.selectById(farmer.getOriginAreaId());
            String city = (area != null && area.getCity() != null && !area.getCity().isEmpty()) ? area.getCity() : "未知";
            java.util.concurrent.ConcurrentLinkedQueue<Long> queue = cityEvents.computeIfAbsent(city, k -> new java.util.concurrent.ConcurrentLinkedQueue<>());
            long ts = System.currentTimeMillis();
            queue.add(ts);
        } catch (Exception e) {
            log.warn("记录农户活跃事件失败 userId={}", userId, e);
        }
    }
    
    // 获取最近N分钟各城市农户活跃度
    @Override
    public java.util.Map<String, Long> getFarmerActivityByCity(int windowMinutes) {
        long cutoff = System.currentTimeMillis() - windowMinutes * 60_000L;
        java.util.Map<String, Long> result = new java.util.HashMap<>();
        for (java.util.Map.Entry<String, java.util.concurrent.ConcurrentLinkedQueue<Long>> entry : cityEvents.entrySet()) {
            String city = entry.getKey();
            java.util.concurrent.ConcurrentLinkedQueue<Long> q = entry.getValue();
            // 清理过期事件
            while (!q.isEmpty() && q.peek() < cutoff) {
                q.poll();
            }
            int size = q.size();
            if (size > 0) {
                result.put(city, (long) size);
            } else {
                // 队列为空则尝试移除该城市，避免内存增长
                cityEvents.remove(city, q);
            }
        }
        return result;
    }
}