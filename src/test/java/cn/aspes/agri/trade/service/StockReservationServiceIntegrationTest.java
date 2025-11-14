package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.dto.UserRegisterRequest;
import cn.aspes.agri.trade.entity.*;
import cn.aspes.agri.trade.enums.ContractStatus;
import cn.aspes.agri.trade.enums.ProductStatus;
import cn.aspes.agri.trade.enums.UserRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
 * 库存预留服务集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StockReservationServiceIntegrationTest {

    @Autowired
    private StockReservationService stockReservationService;

    @Autowired
    private FarmerProductService farmerProductService;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private PurchaseContractService purchaseContractService;

    @Autowired
    private FarmerInfoService farmerInfoService;

    @Autowired
    private UserService userService;

    private Long farmerId;
    private Long purchaserId;
    private Long productId;
    private Long orderId;

    @BeforeEach
    void setUp() {
        // 创建农民用户
        UserRegisterRequest farmerRequest = new UserRegisterRequest();
        farmerRequest.setUsername("farmer_stock" + System.currentTimeMillis());
        farmerRequest.setPassword("password123");
        farmerRequest.setRole(UserRole.FARMER);
        farmerRequest.setContactPerson("张三");
        farmerRequest.setContactPhone("13800138000");
        farmerRequest.setContactEmail("farmer@example.com");
        farmerId = userService.register(farmerRequest);

        // 创建采购方用户
        UserRegisterRequest purchaserRequest = new UserRegisterRequest();
        purchaserRequest.setUsername("purchaser_stock" + System.currentTimeMillis());
        purchaserRequest.setPassword("password123");
        purchaserRequest.setRole(UserRole.PURCHASER);
        purchaserRequest.setContactPerson("李四");
        purchaserRequest.setContactPhone("13800138001");
        purchaserRequest.setContactEmail("purchaser@example.com");
        purchaserId = userService.register(purchaserRequest);

        // 创建农民信息
        FarmerInfo farmerInfo = new FarmerInfo();
        farmerInfo.setUserId(farmerId);
        farmerInfo.setFarmName("示范农场");
        farmerInfo.setOriginAreaId(1);
        farmerInfoService.save(farmerInfo);

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
        Page<PurchaseOrder> page = purchaseOrderService.pageOrders(1, 10, null);
        orderId = page.getRecords().get(0).getId();
    }

    @Test
    void testReserveStock() {
        // 预留库存
        Long reservationId = stockReservationService.reserveStock(orderId, productId, 100);
        assertNotNull(reservationId);

        // 验证预留记录
        StockReservation reservation = stockReservationService.getById(reservationId);
        assertNotNull(reservation);
        assertEquals(orderId, reservation.getOrderId());
        assertEquals(productId, reservation.getProductId());
        assertEquals(100, reservation.getReservedQuantity());
        assertEquals("reserved", reservation.getStatus());
        assertNotNull(reservation.getExpiredTime());
        assertNotNull(reservation.getCreateTime());
    }

    @Test
    void testReserveStockWithInsufficientStock() {
        // 尝试预留超过库存的数量
        assertThrows(Exception.class, () -> {
            stockReservationService.reserveStock(orderId, productId, 2000);
        });
    }

    @Test
    void testReserveStockForNonExistentProduct() {
        // 尝试为不存在的产品预留库存
        assertThrows(Exception.class, () -> {
            stockReservationService.reserveStock(orderId, 99999L, 100);
        });
    }

    @Test
    void testReserveStockDuplicate() {
        // 第一次预留
        Long reservationId = stockReservationService.reserveStock(orderId, productId, 100);
        assertNotNull(reservationId);

        // 尝试重复预留
        assertThrows(Exception.class, () -> {
            stockReservationService.reserveStock(orderId, productId, 50);
        });
    }

    @Test
    void testReleaseReservation() {
        // 预留库存
        Long reservationId = stockReservationService.reserveStock(orderId, productId, 100);

        // 释放预留
        stockReservationService.releaseReservation(orderId, "测试释放");

        // 验证预留已释放
        StockReservation reservation = stockReservationService.getById(reservationId);
        assertEquals("released", reservation.getStatus());
        assertEquals("测试释放", reservation.getReleaseReason());
    }

    @Test
    void testReleaseNonExistentReservation() {
        // 尝试释放不存在的预留，应该不会抛出异常
        assertDoesNotThrow(() -> {
            stockReservationService.releaseReservation(99999L, "测试不存在的预留");
        });
    }

    @Test
    void testReleaseAlreadyReleasedReservation() {
        // 预留库存
        Long reservationId = stockReservationService.reserveStock(orderId, productId, 100);

        // 第一次释放
        stockReservationService.releaseReservation(orderId, "第一次释放");

        // 尝试再次释放，应该不会抛出异常
        assertDoesNotThrow(() -> {
            stockReservationService.releaseReservation(orderId, "第二次释放");
        });
    }

    @Test
    void testConfirmReservation() {
        // 预留库存
        Long reservationId = stockReservationService.reserveStock(orderId, productId, 100);

        // 确认预留
        stockReservationService.confirmReservation(reservationId);

        // 验证预留已确认
        StockReservation reservation = stockReservationService.getById(reservationId);
        assertEquals("confirmed", reservation.getStatus());

        // 验证库存已扣减
        FarmerProduct product = farmerProductService.getById(productId);
        assertEquals(900, product.getStock()); // 原始库存1000，预留100后应剩900
    }

    @Test
    void testConfirmReservationWithInsufficientStock() {
        // 预留库存
        Long reservationId = stockReservationService.reserveStock(orderId, productId, 100);

        // 手动修改产品库存，模拟库存不足
        FarmerProduct product = farmerProductService.getById(productId);
        product.setStock(50); // 设置为小于预留数量
        farmerProductService.updateById(product);

        // 尝试确认预留，应该失败
        assertThrows(Exception.class, () -> {
            stockReservationService.confirmReservation(reservationId);
        });
    }

    @Test
    void testConfirmNonExistentReservation() {
        // 尝试确认不存在的预留，应该抛出异常
        assertThrows(Exception.class, () -> {
            stockReservationService.confirmReservation(99999L);
        });
    }

    @Test
    void testConfirmAlreadyReleasedReservation() {
        // 预留库存
        Long reservationId = stockReservationService.reserveStock(orderId, productId, 100);

        // 释放预留
        stockReservationService.releaseReservation(orderId, "测试释放");

        // 尝试确认已释放的预留，应该失败
        assertThrows(Exception.class, () -> {
            stockReservationService.confirmReservation(reservationId);
        });
    }

    @Test
    void testGetByOrderId() {
        // 预留库存
        stockReservationService.reserveStock(orderId, productId, 100);

        // 根据订单ID获取预留记录
        StockReservation reservation = stockReservationService.getByOrderId(orderId);
        assertNotNull(reservation);
        assertEquals(orderId, reservation.getOrderId());
        assertEquals(productId, reservation.getProductId());
        assertEquals(100, reservation.getReservedQuantity());
        assertEquals("reserved", reservation.getStatus());
    }

    @Test
    void testGetByNonExistentOrderId() {
        // 尝试获取不存在订单的预留记录
        StockReservation reservation = stockReservationService.getByOrderId(99999L);
        assertNull(reservation);
    }

    @Test
    void testGetByOrderIdWithReleasedReservation() {
        // 预留库存
        stockReservationService.reserveStock(orderId, productId, 100);

        // 释放预留
        stockReservationService.releaseReservation(orderId, "测试释放");

        // 尝试获取已释放的预留记录
        StockReservation reservation = stockReservationService.getByOrderId(orderId);
        assertNull(reservation); // 已释放的预留不应该被获取到
    }

    @Test
    void testStockReservationLifecycle() {
        // 1. 预留库存
        Long reservationId = stockReservationService.reserveStock(orderId, productId, 100);
        StockReservation reservation = stockReservationService.getById(reservationId);
        assertEquals("reserved", reservation.getStatus());

        // 2. 确认预留
        stockReservationService.confirmReservation(reservationId);
        reservation = stockReservationService.getById(reservationId);
        assertEquals("confirmed", reservation.getStatus());

        // 验证库存已扣减
        FarmerProduct product = farmerProductService.getById(productId);
        assertEquals(900, product.getStock());
    }

    @Test
    void testMultipleReservationsForSameProduct() {
        // 创建第二个订单
        PurchaseContract contract2 = new PurchaseContract();
        contract2.setPurchaserId(purchaserId);
        contract2.setFarmerId(farmerId);
        contract2.setTotalAmount(new BigDecimal("300.00"));
        contract2.setPaymentTerms("30天内支付");
        contract2.setDeliveryTime(LocalDate.now().plusDays(7));
        contract2.setDeliveryAddress("上海市");
        contract2.setStatus(ContractStatus.SIGNED);
        purchaseContractService.save(contract2);

        purchaseOrderService.createOrderFromContract(contract2.getId());
        Page<PurchaseOrder> page = purchaseOrderService.pageOrders(1, 10, null);
        Long orderId2 = page.getRecords().get(0).getId();

        // 为两个订单分别预留库存
        Long reservationId1 = stockReservationService.reserveStock(orderId, productId, 300);
        Long reservationId2 = stockReservationService.reserveStock(orderId2, productId, 200);

        assertNotNull(reservationId1);
        assertNotNull(reservationId2);

        // 验证两个预留记录都存在
        StockReservation reservation1 = stockReservationService.getById(reservationId1);
        StockReservation reservation2 = stockReservationService.getById(reservationId2);

        assertEquals(300, reservation1.getReservedQuantity());
        assertEquals(200, reservation2.getReservedQuantity());

        // 确认第一个预留
        stockReservationService.confirmReservation(reservationId1);

        // 验证库存已扣减300
        FarmerProduct product = farmerProductService.getById(productId);
        assertEquals(700, product.getStock()); // 原始库存1000，确认300后应剩700

        // 确认第二个预留
        stockReservationService.confirmReservation(reservationId2);

        // 验证库存又扣减200
        product = farmerProductService.getById(productId);
        assertEquals(500, product.getStock()); // 剩余700，再确认200后应剩500
    }
}