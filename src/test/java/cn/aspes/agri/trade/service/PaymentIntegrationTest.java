package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.dto.PaymentRequest;
import cn.aspes.agri.trade.dto.UserRegisterRequest;
import cn.aspes.agri.trade.entity.*;
import cn.aspes.agri.trade.enums.ContractStatus;
import cn.aspes.agri.trade.enums.OrderStatus;
import cn.aspes.agri.trade.enums.PaymentMethod;
import cn.aspes.agri.trade.enums.PaymentStatus;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 支付记录服务集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentIntegrationTest {

    @Autowired
    private PaymentRecordService paymentRecordService;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private PurchaseContractService purchaseContractService;

    @Autowired
    private FarmerProductService farmerProductService;

    @Autowired
    private FarmerInfoService farmerInfoService;

    @Autowired
    private UserService userService;

    private Long farmerId;
    private Long purchaserId;
    private Long orderId;

    @BeforeEach
    void setUp() {
        // 创建农民用户
        UserRegisterRequest farmerRequest = new UserRegisterRequest();
        farmerRequest.setUsername("farmer_pay" + System.currentTimeMillis());
        farmerRequest.setPassword("password123");
        farmerRequest.setRole(UserRole.FARMER);
        farmerRequest.setContactPerson("张三");
        farmerRequest.setContactPhone("13800138000");
        farmerRequest.setContactEmail("farmer@example.com");
        farmerId = userService.register(farmerRequest);

        // 创建采购方用户
        UserRegisterRequest purchaserRequest = new UserRegisterRequest();
        purchaserRequest.setUsername("purchaser_pay" + System.currentTimeMillis());
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
    void testSubmitPayment() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(orderId);
        request.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        request.setAmount(new BigDecimal("500.00"));
        request.setPaymentStage("全款");

        Long paymentId = paymentRecordService.submitPayment(request);
        assertNotNull(paymentId);
        assertTrue(paymentId > 0);

        PaymentRecord payment = paymentRecordService.getById(paymentId);
        assertNotNull(payment);
        assertEquals(orderId, payment.getOrderId());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals(new BigDecimal("500.00"), payment.getAmount());
    }

    @Test
    void testConfirmPayment() {
        // 提交支付
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(orderId);
        request.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        request.setAmount(new BigDecimal("500.00"));
        request.setPaymentStage("全款");

        Long paymentId = paymentRecordService.submitPayment(request);

        // 确认支付
        paymentRecordService.confirmPayment(paymentId, "PAY20240101001");

        PaymentRecord payment = paymentRecordService.getById(paymentId);
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals("PAY20240101001", payment.getPaymentNo());
    }

    @Test
    void testListByOrder() {
        // 提交多笔支付
        for (int i = 0; i < 2; i++) {
            PaymentRequest request = new PaymentRequest();
            request.setOrderId(orderId);
            request.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
            request.setAmount(new BigDecimal("250.00"));
            request.setPaymentStage("分期" + i);

            paymentRecordService.submitPayment(request);
        }

        List<PaymentRecord> payments = paymentRecordService.listByOrder(orderId);
        assertTrue(payments.size() >= 2);

        for (PaymentRecord payment : payments) {
            assertEquals(orderId, payment.getOrderId());
        }
    }

    @Test
    void testPagePayments() {
        // 提交支付
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(orderId);
        request.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        request.setAmount(new BigDecimal("500.00"));
        request.setPaymentStage("全款");

        paymentRecordService.submitPayment(request);

        // 分页查询
        Page<PaymentRecord> page = paymentRecordService.pagePayments(1, 10, orderId, null);
        assertTrue(page.getRecords().size() > 0);

        PaymentRecord payment = page.getRecords().get(0);
        assertEquals(orderId, payment.getOrderId());
    }

    @Test
    void testPagePaymentsByStatus() {
        // 提交支付
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(orderId);
        request.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        request.setAmount(new BigDecimal("500.00"));
        request.setPaymentStage("全款");

        paymentRecordService.submitPayment(request);

        // 按状态查询待支付的
        Page<PaymentRecord> page = paymentRecordService.pagePayments(1, 10, null, PaymentStatus.PENDING.getCode());
        assertTrue(page.getRecords().size() > 0);
    }

    @Test
    void testMarkPaymentFailed() {
        // 提交支付
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(orderId);
        request.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        request.setAmount(new BigDecimal("500.00"));
        request.setPaymentStage("全款");

        Long paymentId = paymentRecordService.submitPayment(request);

        // 标记为失败
        paymentRecordService.markPaymentFailed(paymentId, "余额不足");

        PaymentRecord payment = paymentRecordService.getById(paymentId);
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
    }

    @Test
    void testListMyPayments() {
        // 提交支付
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(orderId);
        request.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        request.setAmount(new BigDecimal("500.00"));
        request.setPaymentStage("全款");

        paymentRecordService.submitPayment(request);

        // 查询用户的支付记录
        Page<PaymentRecord> page = paymentRecordService.listMyPayments(purchaserId, UserRole.PURCHASER.getCode(), 1, 10);
        assertTrue(page.getRecords().size() > 0);
    }

    @Test
    void testDifferentPaymentMethods() {
        // 测试不同的支付方式
        PaymentMethod[] methods = {PaymentMethod.BANK_TRANSFER, PaymentMethod.ALIPAY, PaymentMethod.WECHAT};

        for (int i = 0; i < methods.length; i++) {
            PaymentRequest request = new PaymentRequest();
            request.setOrderId(orderId);
            request.setPaymentMethod(methods[i]);
            request.setAmount(new BigDecimal("100.00"));
            request.setPaymentStage("支付方式测试" + i);

            Long paymentId = paymentRecordService.submitPayment(request);
            assertNotNull(paymentId);

            PaymentRecord payment = paymentRecordService.getById(paymentId);
            assertEquals(methods[i], payment.getPaymentMethod());
        }
    }

    @Test
    void testPartialPayment() {
        // 测试分期支付
        BigDecimal totalAmount = new BigDecimal("500.00");
        BigDecimal installmentAmount = new BigDecimal("250.00");

        // 首期支付
        PaymentRequest request1 = new PaymentRequest();
        request1.setOrderId(orderId);
        request1.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        request1.setAmount(installmentAmount);
        request1.setPaymentStage("首期");

        Long paymentId1 = paymentRecordService.submitPayment(request1);
        paymentRecordService.confirmPayment(paymentId1, "PAY20240101001");

        // 二期支付
        PaymentRequest request2 = new PaymentRequest();
        request2.setOrderId(orderId);
        request2.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        request2.setAmount(installmentAmount);
        request2.setPaymentStage("二期");

        Long paymentId2 = paymentRecordService.submitPayment(request2);
        paymentRecordService.confirmPayment(paymentId2, "PAY20240101002");

        // 验证两笔支付都已完成
        PaymentRecord payment1 = paymentRecordService.getById(paymentId1);
        PaymentRecord payment2 = paymentRecordService.getById(paymentId2);

        assertEquals(PaymentStatus.SUCCESS, payment1.getStatus());
        assertEquals(PaymentStatus.SUCCESS, payment2.getStatus());

        List<PaymentRecord> payments = paymentRecordService.listByOrder(orderId);
        long completedCount = payments.stream()
                .filter(p -> PaymentStatus.SUCCESS == p.getStatus())
                .count();
        assertEquals(2, completedCount);
    }
}
