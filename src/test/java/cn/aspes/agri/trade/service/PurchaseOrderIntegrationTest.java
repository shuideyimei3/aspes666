package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.config.TestDatabaseConfig;
import cn.aspes.agri.trade.dto.PaymentRequest;
import cn.aspes.agri.trade.dto.UserRegisterRequest;
import cn.aspes.agri.trade.entity.FarmerInfo;
import cn.aspes.agri.trade.entity.FarmerProduct;
import cn.aspes.agri.trade.entity.PurchaseContract;
import cn.aspes.agri.trade.entity.PurchaseOrder;
import cn.aspes.agri.trade.entity.PurchaserInfo;
import cn.aspes.agri.trade.enums.ContractStatus;
import cn.aspes.agri.trade.enums.OrderStatus;
import cn.aspes.agri.trade.enums.PaymentMethod;
import cn.aspes.agri.trade.enums.ProductStatus;
import cn.aspes.agri.trade.enums.UserRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 购买订单服务集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestDatabaseConfig.class)
@Transactional
class PurchaseOrderIntegrationTest {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private PurchaseContractService purchaseContractService;

    @Autowired
    private FarmerProductService farmerProductService;

    @Autowired
    private FarmerInfoService farmerInfoService;

    @Autowired
    private PurchaserInfoService purchaserInfoService;

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentRecordService paymentRecordService;

    private Long farmerId;
    private Long purchaserId;
    private Long purchaserInfoId;
    private Long productId;
    private Long contractId;

    @BeforeEach
    void setUp() {
        // 创建农民用户
        UserRegisterRequest farmerRequest = new UserRegisterRequest();
        farmerRequest.setUsername("farmer" + System.currentTimeMillis());
        farmerRequest.setPassword("password123");
        farmerRequest.setRole(UserRole.FARMER);
        farmerRequest.setContactPerson("张三");
        farmerRequest.setContactPhone("13800138000");
        farmerRequest.setContactEmail("farmer@example.com");
        farmerId = userService.register(farmerRequest);

        // 创建采购方用户
        UserRegisterRequest purchaserRequest = new UserRegisterRequest();
        purchaserRequest.setUsername("purchaser" + System.currentTimeMillis());
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
        farmerInfo.setOriginAreaId(1); // 设置必需的origin_area_id
        farmerInfoService.save(farmerInfo);
        
        // 创建采购方信息
        PurchaserInfo purchaserInfo = new PurchaserInfo();
        purchaserInfo.setUserId(purchaserId);
        purchaserInfo.setCompanyName("测试采购公司");
        purchaserInfo.setBusinessLicense("http://example.com/license");
        purchaserInfoService.save(purchaserInfo);
        purchaserInfoId = purchaserInfo.getId(); // 保存采购方信息ID

        // 创建产品
        FarmerProduct product = new FarmerProduct();
        product.setFarmerId(farmerId);
        product.setCategoryId(1L); // 设置必需的category_id
        product.setOriginAreaId(1); // 设置必需的origin_area_id
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
        contract.setContractNo("CT" + System.currentTimeMillis()); // 设置必需的合同编号
        contract.setDockingId(1L); // 设置必需的对接ID
        contract.setPurchaserId(purchaserInfoId); // 使用采购方信息ID
        contract.setFarmerId(farmerId);
        Map<String, Object> productInfo = new HashMap<>();
        productInfo.put("productId", productId); // 添加产品ID，这是必需的
        productInfo.put("name", "水稻");
        productInfo.put("spec", "标准规格");
        productInfo.put("quantity", 100);
        productInfo.put("unit", "公斤");
        productInfo.put("price", 5.00);
        contract.setProductInfo(productInfo);
        contract.setTotalAmount(new BigDecimal("500.00"));
        contract.setPaymentTerms("30天内支付");
        contract.setDeliveryTime(LocalDate.now().plusDays(7));
        contract.setDeliveryAddress("浙江省杭州市");
        contract.setStatus(ContractStatus.SIGNED);
        purchaseContractService.save(contract);
        contractId = contract.getId();
    }

    @Test
    void testCreateOrderFromContract() {
        purchaseOrderService.createOrderFromContract(contractId);

        Page<PurchaseOrder> page = purchaseOrderService.pageOrders(1, 10, null);
        assertTrue(page.getRecords().size() > 0);

        PurchaseOrder order = page.getRecords().get(0);
        assertNotNull(order);
        assertEquals(contractId, order.getContractId());
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void testInspectOrder() {
        // 创建订单
        purchaseOrderService.createOrderFromContract(contractId);
        Page<PurchaseOrder> page = purchaseOrderService.pageOrders(1, 10, null);
        Long orderId = page.getRecords().get(0).getId();

        // 订单验收
        purchaseOrderService.inspectOrder(orderId, 100, "质量良好");

        PurchaseOrder order = purchaseOrderService.getById(orderId);
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertEquals(100, order.getActualQuantity());
        assertEquals("质量良好", order.getInspectionResult());
    }

    @Test
    void testCompleteOrder() {
        // 创建订单
        purchaseOrderService.createOrderFromContract(contractId);
        Page<PurchaseOrder> page = purchaseOrderService.pageOrders(1, 10, null);
        Long orderId = page.getRecords().get(0).getId();

        // 验收订单
        purchaseOrderService.inspectOrder(orderId, 100, "质量良好");

        // 提交支付
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(orderId);
        paymentRequest.setPaymentStage("final");
        paymentRequest.setAmount(new BigDecimal("500.00"));
        paymentRequest.setPaymentMethod(PaymentMethod.ALIPAY);
        Long paymentId = paymentRecordService.submitPayment(paymentRequest);
        
        // 确认支付成功
        paymentRecordService.confirmPayment(paymentId, "PAY" + System.currentTimeMillis());

        // 完成订单
        purchaseOrderService.completeOrder(orderId);

        PurchaseOrder order = purchaseOrderService.getById(orderId);
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    void testCancelOrder() {
        // 创建订单
        purchaseOrderService.createOrderFromContract(contractId);
        Page<PurchaseOrder> page = purchaseOrderService.pageOrders(1, 10, null);
        Long orderId = page.getRecords().get(0).getId();

        // 取消订单
        purchaseOrderService.cancelOrder(orderId, "产品质量问题");

        PurchaseOrder order = purchaseOrderService.getById(orderId);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void testPageOrders() {
        // 创建多个订单，需要创建不同的合同，因为每个合同只能生成一个订单
        for (int i = 0; i < 3; i++) {
            // 创建新的采购合同
            PurchaseContract contract = new PurchaseContract();
            contract.setContractNo("CT" + System.currentTimeMillis() + i); // 设置必需的合同编号
            contract.setDockingId(1L + i); // 设置不同的对接ID
            contract.setPurchaserId(purchaserInfoId); // 使用采购方信息ID
            contract.setFarmerId(farmerId);
            Map<String, Object> productInfo = new HashMap<>();
            productInfo.put("productId", productId); // 添加产品ID，这是必需的
            productInfo.put("name", "水稻");
            productInfo.put("spec", "标准规格");
            productInfo.put("quantity", 100);
            productInfo.put("unit", "公斤");
            productInfo.put("price", 5.00);
            contract.setProductInfo(productInfo);
            contract.setTotalAmount(new BigDecimal("500.00"));
            contract.setPaymentTerms("30天内支付");
            contract.setDeliveryTime(LocalDate.now().plusDays(7));
            contract.setDeliveryAddress("浙江省杭州市");
            contract.setStatus(ContractStatus.SIGNED);
            purchaseContractService.save(contract);
            
            // 从合同创建订单
            purchaseOrderService.createOrderFromContract(contract.getId());
        }

        Page<PurchaseOrder> page = purchaseOrderService.pageOrders(1, 10, null);
        assertTrue(page.getRecords().size() >= 3);
    }

    @Test
    void testPageOrdersByStatus() {
        // 创建订单
        purchaseOrderService.createOrderFromContract(contractId);
        Page<PurchaseOrder> createdPage = purchaseOrderService.pageOrders(1, 10, OrderStatus.PENDING.getCode());
        assertTrue(createdPage.getRecords().size() > 0);
    }

    @Test
    void testListMyOrders() {
        // 创建订单
        purchaseOrderService.createOrderFromContract(contractId);

        Page<PurchaseOrder> page = purchaseOrderService.listMyOrders(purchaserId, UserRole.PURCHASER.getCode(), 1, 10);
        assertTrue(page.getRecords().size() > 0);

        PurchaseOrder order = page.getRecords().get(0);
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void testGetOrderDetail() {
        // 创建订单
        purchaseOrderService.createOrderFromContract(contractId);
        Page<PurchaseOrder> page = purchaseOrderService.pageOrders(1, 10, null);
        Long orderId = page.getRecords().get(0).getId();

        PurchaseOrder order = purchaseOrderService.getOrderDetail(orderId);
        assertNotNull(order);
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(contractId, order.getContractId());
    }

    @Test
    void testOrderLifecycle() {
        // 创建订单
        purchaseOrderService.createOrderFromContract(contractId);
        Page<PurchaseOrder> page = purchaseOrderService.pageOrders(1, 10, null);
        Long orderId = page.getRecords().get(0).getId();

        // 验证初始状态
        PurchaseOrder order = purchaseOrderService.getById(orderId);
        assertEquals(OrderStatus.PENDING, order.getStatus());

        // 验收订单
        purchaseOrderService.inspectOrder(orderId, 100, "质量良好");
        order = purchaseOrderService.getById(orderId);
        assertEquals(OrderStatus.DELIVERED, order.getStatus());

        // 提交支付
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(orderId);
        paymentRequest.setPaymentStage("final");
        paymentRequest.setAmount(new BigDecimal("500.00"));
        paymentRequest.setPaymentMethod(PaymentMethod.ALIPAY);
        Long paymentId = paymentRecordService.submitPayment(paymentRequest);
        
        // 确认支付成功
        paymentRecordService.confirmPayment(paymentId, "PAY" + System.currentTimeMillis());

        // 完成订单
        purchaseOrderService.completeOrder(orderId);
        order = purchaseOrderService.getById(orderId);
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }
}