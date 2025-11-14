package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.dto.UserRegisterRequest;
import cn.aspes.agri.trade.entity.*;
import cn.aspes.agri.trade.enums.ContractStatus;
import cn.aspes.agri.trade.enums.OrderStatus;
import cn.aspes.agri.trade.enums.ProductStatus;
import cn.aspes.agri.trade.enums.UserRole;
import cn.aspes.agri.trade.vo.StatisticsVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 统计分析服务集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StatisticsServiceIntegrationTest {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private UserService userService;

    @Autowired
    private FarmerInfoService farmerInfoService;

    @Autowired
    private PurchaserInfoService purchaserInfoService;

    @Autowired
    private FarmerProductService farmerProductService;

    @Autowired
    private PurchaseDemandService purchaseDemandService;

    @Autowired
    private PurchaseContractService purchaseContractService;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private CooperationReviewService cooperationReviewService;

    private Long farmerId;
    private Long purchaserId;
    private Long productId;
    private Long orderId;

    @BeforeEach
    void setUp() {
        // 创建农民用户
        UserRegisterRequest farmerRequest = new UserRegisterRequest();
        farmerRequest.setUsername("farmer_stats" + System.currentTimeMillis());
        farmerRequest.setPassword("password123");
        farmerRequest.setRole(UserRole.FARMER);
        farmerRequest.setContactPerson("张三");
        farmerRequest.setContactPhone("13800138000");
        farmerRequest.setContactEmail("farmer@example.com");
        farmerId = userService.register(farmerRequest);

        // 创建农民信息
        FarmerInfo farmerInfo = new FarmerInfo();
        farmerInfo.setUserId(farmerId);
        farmerInfo.setFarmName("示范农场");
        farmerInfo.setOriginAreaId(1);
        farmerInfoService.save(farmerInfo);

        // 创建采购方用户
        UserRegisterRequest purchaserRequest = new UserRegisterRequest();
        purchaserRequest.setUsername("purchaser_stats" + System.currentTimeMillis());
        purchaserRequest.setPassword("password123");
        purchaserRequest.setRole(UserRole.PURCHASER);
        purchaserRequest.setContactPerson("李四");
        purchaserRequest.setContactPhone("13800138001");
        purchaserRequest.setContactEmail("purchaser@example.com");
        purchaserId = userService.register(purchaserRequest);

        // 创建采购方信息
        PurchaserInfo purchaserInfo = new PurchaserInfo();
        purchaserInfo.setUserId(purchaserId);
        purchaserInfo.setCompanyName("采购公司");
        purchaserInfoService.save(purchaserInfo);

        // 创建产品
        FarmerProduct product = new FarmerProduct();
        product.setFarmerId(farmerId);
        product.setCategoryId(1L);
        product.setOriginAreaId(1);
        product.setName("水稻");
        product.setSpec("标准规格");
        product.setUnit("公斤");
        product.setPrice(new BigDecimal("5.00"));
        product.setMinPurchase(10);
        product.setStock(1000);
        product.setStatus(ProductStatus.ON_SALE);
        farmerProductService.save(product);
        productId = product.getId();

        // 创建采购合同
        PurchaseContract contract = new PurchaseContract();
        contract.setPurchaserId(purchaserId);
        contract.setFarmerId(farmerId);
        contract.setTotalAmount(new BigDecimal("500.00"));
        contract.setPaymentTerms("30天内支付");
        contract.setDeliveryTime(LocalDate.now().plusDays(7));
        contract.setDeliveryAddress("浙江省杭州市");
        contract.setStatus(ContractStatus.SIGNED);
        purchaseContractService.save(contract);

        // 创建订单
        purchaseOrderService.createOrderFromContract(contract.getId());
        orderId = purchaseOrderService.pageOrders(1, 10, null).getRecords().get(0).getId();
    }

    @Test
    void testGetUserOrderStatsForFarmer() {
        // 获取农户订单统计
        StatisticsVO.UserOrderStats stats = statisticsService.getUserOrderStats(farmerId, "farmer");

        assertNotNull(stats);
        assertEquals(0L, stats.getCompletedOrders()); // 初始状态下没有已完成订单
        assertEquals(1L, stats.getTotalOrders()); // 有一个订单
        assertEquals(BigDecimal.ZERO, stats.getTotalAmount()); // 订单未完成，金额为0
        assertEquals(BigDecimal.ZERO, stats.getAverageAmount());
        assertEquals(0.0, stats.getCompletionRate());
    }

    @Test
    void testGetUserOrderStatsForPurchaser() {
        // 获取采购方订单统计
        StatisticsVO.UserOrderStats stats = statisticsService.getUserOrderStats(purchaserId, "purchaser");

        assertNotNull(stats);
        assertEquals(0L, stats.getCompletedOrders()); // 初始状态下没有已完成订单
        assertEquals(1L, stats.getTotalOrders()); // 有一个订单
        assertEquals(BigDecimal.ZERO, stats.getTotalAmount()); // 订单未完成，金额为0
        assertEquals(BigDecimal.ZERO, stats.getAverageAmount());
        assertEquals(0.0, stats.getCompletionRate());
    }

    @Test
    void testGetUserOrderStatsForNonExistentUser() {
        // 获取不存在用户的订单统计
        StatisticsVO.UserOrderStats stats = statisticsService.getUserOrderStats(99999L, "farmer");

        assertNotNull(stats);
        assertEquals(0L, stats.getCompletedOrders());
        assertEquals(0L, stats.getTotalOrders());
        assertEquals(BigDecimal.ZERO, stats.getTotalAmount());
        assertEquals(BigDecimal.ZERO, stats.getAverageAmount());
        assertEquals(0.0, stats.getCompletionRate());
    }

    @Test
    void testGetProductSalesStats() {
        // 获取产品销售统计
        StatisticsVO.ProductSalesStats stats = statisticsService.getProductSalesStats(productId);

        assertNotNull(stats);
        assertEquals("水稻", stats.getProductName());
        assertEquals(0, stats.getSalesCount()); // 初始状态下没有销售
        assertEquals(BigDecimal.ZERO, stats.getSalesAmount());
        assertEquals(0, stats.getReviewCount());
        assertEquals(0.0, stats.getAverageRating());
    }

    @Test
    void testGetProductSalesStatsForNonExistentProduct() {
        // 获取不存在产品的销售统计
        StatisticsVO.ProductSalesStats stats = statisticsService.getProductSalesStats(99999L);

        assertNotNull(stats);
        assertEquals("", stats.getProductName());
        assertEquals(0, stats.getSalesCount());
        assertEquals(BigDecimal.ZERO, stats.getSalesAmount());
        assertEquals(0, stats.getReviewCount());
        assertEquals(0.0, stats.getAverageRating());
    }

    @Test
    void testGetPlatformStats() {
        // 获取平台统计数据
        StatisticsVO.PlatformStats stats = statisticsService.getPlatformStats();

        assertNotNull(stats);
        assertTrue(stats.getTotalUsers() >= 2); // 至少有农户和采购方两个用户
        assertTrue(stats.getTotalFarmers() >= 1); // 至少有一个农户
        assertTrue(stats.getTotalPurchasers() >= 1); // 至少有一个采购方
        assertTrue(stats.getTotalProducts() >= 1); // 至少有一个产品
        assertTrue(stats.getTotalOrders() >= 1); // 至少有一个订单
        assertTrue(stats.getTotalTransactionAmount().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(stats.getActiveUsersToday() >= 0);
    }

    @Test
    void testGetPurchaserStats() {
        // 获取采购方统计数据
        PurchaserInfo purchaserInfo = purchaserInfoService.getByUserId(purchaserId);
        StatisticsVO.PurchaserStats stats = statisticsService.getPurchaserStats(purchaserInfo.getId());

        assertNotNull(stats);
        assertEquals(0L, stats.getDemandCount()); // 初始状态下没有需求
        assertEquals(1L, stats.getOrdersCount()); // 有一个订单
        assertTrue(stats.getTotalPurchase().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(stats.getAverageOrderAmount() >= 0);
    }

    @Test
    void testGetPurchaserStatsForNonExistentPurchaser() {
        // 获取不存在采购方的统计数据
        StatisticsVO.PurchaserStats stats = statisticsService.getPurchaserStats(99999L);

        assertNotNull(stats);
        assertEquals(0L, stats.getDemandCount());
        assertEquals(0L, stats.getOrdersCount());
        assertEquals(BigDecimal.ZERO, stats.getTotalPurchase());
        assertEquals(0.0, stats.getAverageOrderAmount());
    }

    @Test
    void testGetFarmerStats() {
        // 获取农户统计数据
        FarmerInfo farmerInfo = farmerInfoService.getByUserId(farmerId);
        StatisticsVO.FarmerStats stats = statisticsService.getFarmerStats(farmerInfo.getId());

        assertNotNull(stats);
        assertEquals(1, stats.getProductCount()); // 有一个产品
        assertEquals(0L, stats.getSalesOrders()); // 初始状态下没有销售订单
        assertTrue(stats.getTotalSales().compareTo(BigDecimal.ZERO) >= 0);
        assertEquals(0.0, stats.getAverageRating()); // 初始状态下没有评价
    }

    @Test
    void testGetFarmerStatsForNonExistentFarmer() {
        // 获取不存在农户的统计数据
        StatisticsVO.FarmerStats stats = statisticsService.getFarmerStats(99999L);

        assertNotNull(stats);
        assertEquals(0, stats.getProductCount());
        assertEquals(0L, stats.getSalesOrders());
        assertEquals(BigDecimal.ZERO, stats.getTotalSales());
        assertEquals(0.0, stats.getAverageRating());
    }

    @Test
    void testStatisticsWithCompletedOrder() {
        // 完成订单
        PurchaseOrder order = purchaseOrderService.getById(orderId);
        order.setStatus(OrderStatus.COMPLETED);
        order.setActualAmount(new BigDecimal("500.00"));
        purchaseOrderService.updateById(order);

        // 获取农户订单统计
        StatisticsVO.UserOrderStats farmerStats = statisticsService.getUserOrderStats(farmerId, "farmer");
        assertEquals(1L, farmerStats.getCompletedOrders());
        assertEquals(1L, farmerStats.getTotalOrders());
        assertEquals(new BigDecimal("500.00"), farmerStats.getTotalAmount());
        assertEquals(new BigDecimal("500.00"), farmerStats.getAverageAmount());
        assertEquals(100.0, farmerStats.getCompletionRate());

        // 获取采购方订单统计
        StatisticsVO.UserOrderStats purchaserStats = statisticsService.getUserOrderStats(purchaserId, "purchaser");
        assertEquals(1L, purchaserStats.getCompletedOrders());
        assertEquals(1L, purchaserStats.getTotalOrders());
        assertEquals(new BigDecimal("500.00"), purchaserStats.getTotalAmount());
        assertEquals(new BigDecimal("500.00"), purchaserStats.getAverageAmount());
        assertEquals(100.0, purchaserStats.getCompletionRate());

        // 获取产品销售统计
        StatisticsVO.ProductSalesStats productStats = statisticsService.getProductSalesStats(productId);
        assertEquals("水稻", productStats.getProductName());
        assertEquals(100, productStats.getSalesCount()); // 假设订单中有100公斤产品
        assertEquals(new BigDecimal("500.00"), productStats.getSalesAmount());
        assertEquals(0, productStats.getReviewCount());
        assertEquals(0.0, productStats.getAverageRating());

        // 获取农户统计
        FarmerInfo farmerInfo = farmerInfoService.getByUserId(farmerId);
        StatisticsVO.FarmerStats farmerFullStats = statisticsService.getFarmerStats(farmerInfo.getId());
        assertEquals(1, farmerFullStats.getProductCount());
        assertEquals(1L, farmerFullStats.getSalesOrders());
        assertEquals(new BigDecimal("500.00"), farmerFullStats.getTotalSales());
        assertEquals(0.0, farmerFullStats.getAverageRating());

        // 获取采购方统计
        PurchaserInfo purchaserInfo = purchaserInfoService.getByUserId(purchaserId);
        StatisticsVO.PurchaserStats purchaserFullStats = statisticsService.getPurchaserStats(purchaserInfo.getId());
        assertEquals(0L, purchaserFullStats.getDemandCount());
        assertEquals(1L, purchaserFullStats.getOrdersCount());
        assertEquals(new BigDecimal("500.00"), purchaserFullStats.getTotalPurchase());
        assertEquals(500.0, purchaserFullStats.getAverageOrderAmount());
    }

    @Test
    void testStatisticsWithReview() {
        // 完成订单
        PurchaseOrder order = purchaseOrderService.getById(orderId);
        order.setStatus(OrderStatus.COMPLETED);
        order.setActualAmount(new BigDecimal("500.00"));
        purchaseOrderService.updateById(order);

        // 添加评价
        FarmerInfo farmerInfo = farmerInfoService.getByUserId(farmerId);
        cooperationReviewService.submitReview(orderId, purchaserId, farmerInfo.getId(), "farmer", 5, "非常好");

        // 获取产品销售统计
        StatisticsVO.ProductSalesStats productStats = statisticsService.getProductSalesStats(productId);
        assertEquals("水稻", productStats.getProductName());
        assertEquals(100, productStats.getSalesCount()); // 假设订单中有100公斤产品
        assertEquals(new BigDecimal("500.00"), productStats.getSalesAmount());
        assertEquals(1, productStats.getReviewCount());
        assertEquals(5.0, productStats.getAverageRating());

        // 获取农户统计
        StatisticsVO.FarmerStats farmerStats = statisticsService.getFarmerStats(farmerInfo.getId());
        assertEquals(1, farmerStats.getProductCount());
        assertEquals(1L, farmerStats.getSalesOrders());
        assertEquals(new BigDecimal("500.00"), farmerStats.getTotalSales());
        assertEquals(5.0, farmerStats.getAverageRating());
    }

    @Test
    void testStatisticsWithDemand() {
        // 创建采购需求
        PurchaseDemand demand = new PurchaseDemand();
        demand.setPurchaserId(purchaserId);
        demand.setProductName("小麦");
        demand.setSpec("优质");
        demand.setUnit("吨");
        demand.setQuantity(10);
        demand.setPriceRange("2000-2500");
        demand.setDeliveryTime(LocalDate.now().plusDays(15));
        demand.setDeliveryAddress("江苏省南京市");
        demand.setDescription("需要优质小麦");
        purchaseDemandService.save(demand);

        // 获取采购方统计
        PurchaserInfo purchaserInfo = purchaserInfoService.getByUserId(purchaserId);
        StatisticsVO.PurchaserStats stats = statisticsService.getPurchaserStats(purchaserInfo.getId());
        assertEquals(1L, stats.getDemandCount()); // 有一个需求
        assertEquals(1L, stats.getOrdersCount()); // 有一个订单
        assertTrue(stats.getTotalPurchase().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(stats.getAverageOrderAmount() >= 0);
    }
}