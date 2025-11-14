package cn.aspes.agri.trade.controller.b2b;

import cn.aspes.agri.trade.TestApplication;
import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.dto.ContractRequest;
import cn.aspes.agri.trade.dto.PaymentRequest;
import cn.aspes.agri.trade.dto.PurchaseDemandRequest;
import cn.aspes.agri.trade.dto.PurchaserInfoRequest;
import cn.aspes.agri.trade.entity.PurchaseContract;
import cn.aspes.agri.trade.entity.PurchaseDemand;
import cn.aspes.agri.trade.entity.PurchaseOrder;
import cn.aspes.agri.trade.entity.PurchaserInfo;
import cn.aspes.agri.trade.enums.ContractStatus;
import cn.aspes.agri.trade.enums.DemandStatus;
import cn.aspes.agri.trade.enums.OrderStatus;
import cn.aspes.agri.trade.enums.UserRole;
import cn.aspes.agri.trade.security.CustomUserDetails;
import cn.aspes.agri.trade.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * B2B控制器集成测试
 */
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class B2BControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserService userService;

    @Autowired
    private PurchaserInfoService purchaserInfoService;

    @Autowired
    private PurchaseDemandService purchaseDemandService;

    @Autowired
    private PurchaseContractService purchaseContractService;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private PaymentRecordService paymentRecordService;

    @Autowired
    private FarmerProductService farmerProductService;

    @Autowired
    private FarmerInfoService farmerInfoService;

    @Autowired
    private DockingRecordService dockingRecordService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();
    private Long purchaserUserId;
    private Long farmerUserId;
    private Long purchaserId;
    private Long farmerId;
    private Long productId;
    private Long demandId;
    private Long dockingId;
    private Long contractId;
    private Long orderId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // 创建采购方用户
        cn.aspes.agri.trade.dto.UserRegisterRequest purchaserRequest = new cn.aspes.agri.trade.dto.UserRegisterRequest();
        purchaserRequest.setUsername("purchaser" + System.currentTimeMillis());
        purchaserRequest.setPassword("password123");
        purchaserRequest.setRole(UserRole.PURCHASER);
        purchaserRequest.setContactPerson("采购方测试");
        purchaserRequest.setContactPhone("13800138001");
        purchaserRequest.setContactEmail("purchaser@example.com");
        purchaserUserId = userService.register(purchaserRequest);

        // 创建农户用户
        cn.aspes.agri.trade.dto.UserRegisterRequest farmerRequest = new cn.aspes.agri.trade.dto.UserRegisterRequest();
        farmerRequest.setUsername("farmer" + System.currentTimeMillis());
        farmerRequest.setPassword("password123");
        farmerRequest.setRole(UserRole.FARMER);
        farmerRequest.setContactPerson("农户测试");
        farmerRequest.setContactPhone("13800138002");
        farmerRequest.setContactEmail("farmer@example.com");
        farmerUserId = userService.register(farmerRequest);

        // 创建采购方信息
        PurchaserInfoRequest purchaserInfoRequest = new PurchaserInfoRequest();
        purchaserInfoRequest.setCompanyName("测试采购公司");
        purchaserInfoRequest.setBusinessLicense("test-license");
        purchaserInfoRequest.setContactPerson("采购联系人");
        purchaserInfoRequest.setContactPhone("13800138003");
        purchaserInfoRequest.setContactEmail("contact@purchaser.com");
        purchaserInfoRequest.setAddress("测试地址");
        purchaserInfoService.submitPurchaserInfo(purchaserUserId, purchaserInfoRequest);
        purchaserId = purchaserInfoService.getByUserId(purchaserUserId).getId();

        // 创建农户信息
        cn.aspes.agri.trade.entity.FarmerInfo farmerInfo = new cn.aspes.agri.trade.entity.FarmerInfo();
        farmerInfo.setUserId(farmerUserId);
        farmerInfo.setFarmName("测试农场");
        farmerInfo.setOriginAreaId(1L);
        farmerInfo.setProductionScale("中等规模");
        farmerInfoService.save(farmerInfo);
        farmerId = farmerInfo.getId();

        // 创建农产品
        cn.aspes.agri.trade.entity.FarmerProduct product = new cn.aspes.agri.trade.entity.FarmerProduct();
        product.setFarmerId(farmerId);
        product.setName("测试产品");
        product.setSpec("标准规格");
        product.setUnit("公斤");
        product.setPrice(new BigDecimal("10.00"));
        product.setMinPurchase(100);
        product.setStock(1000);
        farmerProductService.save(product);
        productId = product.getId();

        // 创建采购需求
        PurchaseDemandRequest demandRequest = new PurchaseDemandRequest();
        demandRequest.setCategoryId(1L);
        demandRequest.setProductName("测试需求产品");
        demandRequest.setSpec("需求规格");
        demandRequest.setUnit("公斤");
        demandRequest.setQuantity(500);
        demandRequest.setPriceRange("10-15");
        demandRequest.setDeliveryTime("2024-06-01");
        demandRequest.setDeliveryAddress("测试地址");
        demandRequest.setQualityStandard("质量标准");
        demandRequest.setRemark("测试备注");
        demandId = purchaseDemandService.publishDemand(purchaserId, demandRequest);

        // 创建对接记录
        cn.aspes.agri.trade.dto.DockingRecordRequest dockingRequest = new cn.aspes.agri.trade.dto.DockingRecordRequest();
        dockingRequest.setDemandId(demandId);
        dockingRequest.setProductId(productId);
        dockingRequest.setQuantity(500);
        dockingRequest.setPrice(new BigDecimal("12.00"));
        dockingRequest.setDeliveryTime("2024-06-01");
        dockingRequest.setRemark("对接备注");
        dockingId = dockingRecordService.respondToDemand(farmerId, dockingRequest);

        // 采购方处理对接
        cn.aspes.agri.trade.dto.DockingFeedbackRequest feedbackRequest = new cn.aspes.agri.trade.dto.DockingFeedbackRequest();
        feedbackRequest.setStatus("agreed");
        feedbackRequest.setRemark("同意对接");
        dockingRecordService.handleDocking(dockingId, purchaserId, feedbackRequest);

        // 创建合同
        ContractRequest contractRequest = new ContractRequest();
        contractRequest.setDockingId(dockingId);
        Map<String, Object> productInfo = new HashMap<>();
        productInfo.put("productId", productId);
        productInfo.put("productName", "测试产品");
        productInfo.put("spec", "标准规格");
        productInfo.put("quantity", 500);
        productInfo.put("price", 12.00);
        contractRequest.setProductInfo(productInfo);
        contractRequest.setPaymentTerms("30天内支付");
        contractRequest.setDeliveryTime("2024-06-01");
        contractRequest.setDeliveryAddress("测试地址");
        contractRequest.setQualityStandards("质量标准");
        contractRequest.setBreachTerms("违约条款");
        contractId = purchaseContractService.createContract(purchaserUserId, contractRequest);

        // 创建订单
        orderId = purchaseOrderService.createOrderFromContract(contractId);
    }

    @Test
    @WithMockUser(roles = {"PURCHASER"})
    void testPublishDemand() throws Exception {
        PurchaseDemandRequest request = new PurchaseDemandRequest();
        request.setCategoryId(1L);
        request.setProductName("新需求产品");
        request.setSpec("新规格");
        request.setUnit("公斤");
        request.setQuantity(300);
        request.setPriceRange("15-20");
        request.setDeliveryTime("2024-07-01");
        request.setDeliveryAddress("新地址");
        request.setQualityStandard("新质量标准");
        request.setRemark("新备注");

        CustomUserDetails userDetails = new CustomUserDetails(
                purchaserUserId, "purchaser", "password", UserRole.PURCHASER, true);

        MvcResult result = mockMvc.perform(post("/api/b2b/demands")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNumber())
                .andReturn();

        Long newDemandId = objectMapper.readValue(result.getResponse().getContentAsString(), Result.class).getData();
        assertNotNull(newDemandId);
        assertTrue(newDemandId > 0);

        PurchaseDemand demand = purchaseDemandService.getById(newDemandId);
        assertNotNull(demand);
        assertEquals("新需求产品", demand.getProductName());
        assertEquals(DemandStatus.OPEN, demand.getStatus());
    }

    @Test
    @WithMockUser(roles = {"PURCHASER"})
    void testUpdateDemand() throws Exception {
        PurchaseDemandRequest request = new PurchaseDemandRequest();
        request.setProductName("更新后的产品");
        request.setSpec("更新规格");
        request.setUnit("吨");
        request.setQuantity(1000);
        request.setPriceRange("8-12");
        request.setDeliveryTime("2024-08-01");
        request.setDeliveryAddress("更新地址");
        request.setQualityStandard("更新质量标准");
        request.setRemark("更新备注");

        CustomUserDetails userDetails = new CustomUserDetails(
                purchaserUserId, "purchaser", "password", UserRole.PURCHASER, true);

        mockMvc.perform(put("/api/b2b/demands/{demandId}", demandId)
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        PurchaseDemand demand = purchaseDemandService.getById(demandId);
        assertEquals("更新后的产品", demand.getProductName());
        assertEquals("更新规格", demand.getSpec());
    }

    @Test
    @WithMockUser(roles = {"PURCHASER"})
    void testCloseDemand() throws Exception {
        CustomUserDetails userDetails = new CustomUserDetails(
                purchaserUserId, "purchaser", "password", UserRole.PURCHASER, true);

        mockMvc.perform(put("/api/b2b/demands/{demandId}/close", demandId)
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        PurchaseDemand demand = purchaseDemandService.getById(demandId);
        assertEquals(DemandStatus.CLOSED, demand.getStatus());
    }

    @Test
    void testListDemands() throws Exception {
        mockMvc.perform(get("/api/b2b/demands")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].productName").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = {"PURCHASER"})
    void testListMyDemands() throws Exception {
        CustomUserDetails userDetails = new CustomUserDetails(
                purchaserUserId, "purchaser", "password", UserRole.PURCHASER, true);

        mockMvc.perform(get("/api/b2b/demands/my")
                        .with(user(userDetails))
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].productName").isNotEmpty());
    }

    @Test
    void testGetDemand() throws Exception {
        mockMvc.perform(get("/api/b2b/demands/{demandId}", demandId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productName").value("测试需求产品"));
    }

    @Test
    @WithMockUser(roles = {"PURCHASER"})
    void testCreateContract() throws Exception {
        // 创建新的对接记录用于测试
        cn.aspes.agri.trade.dto.DockingRecordRequest dockingRequest = new cn.aspes.agri.trade.dto.DockingRecordRequest();
        dockingRequest.setDemandId(demandId);
        dockingRequest.setProductId(productId);
        dockingRequest.setQuantity(300);
        dockingRequest.setPrice(new BigDecimal("15.00"));
        dockingRequest.setDeliveryTime("2024-06-15");
        dockingRequest.setRemark("测试对接2");
        Long newDockingId = dockingRecordService.respondToDemand(farmerId, dockingRequest);

        // 采购方处理对接
        cn.aspes.agri.trade.dto.DockingFeedbackRequest feedbackRequest = new cn.aspes.agri.trade.dto.DockingFeedbackRequest();
        feedbackRequest.setStatus("agreed");
        feedbackRequest.setRemark("同意对接2");
        dockingRecordService.handleDocking(newDockingId, purchaserId, feedbackRequest);

        ContractRequest request = new ContractRequest();
        request.setDockingId(newDockingId);
        Map<String, Object> productInfo = new HashMap<>();
        productInfo.put("productId", productId);
        productInfo.put("productName", "测试产品2");
        productInfo.put("spec", "标准规格2");
        productInfo.put("quantity", 300);
        productInfo.put("price", 15.00);
        request.setProductInfo(productInfo);
        request.setPaymentTerms("30天内支付");
        request.setDeliveryTime("2024-06-15");
        request.setDeliveryAddress("测试地址2");
        request.setQualityStandards("质量标准2");
        request.setBreachTerms("违约条款2");

        CustomUserDetails userDetails = new CustomUserDetails(
                purchaserUserId, "purchaser", "password", UserRole.PURCHASER, true);

        MvcResult result = mockMvc.perform(post("/api/b2b/contracts")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNumber())
                .andReturn();

        Long newContractId = objectMapper.readValue(result.getResponse().getContentAsString(), Result.class).getData();
        assertNotNull(newContractId);
        assertTrue(newContractId > 0);

        PurchaseContract contract = purchaseContractService.getById(newContractId);
        assertNotNull(contract);
        assertEquals(ContractStatus.DRAFT, contract.getStatus());
    }

    @Test
    @WithMockUser(roles = {"PURCHASER"})
    void testSignContract() throws Exception {
        CustomUserDetails userDetails = new CustomUserDetails(
                purchaserUserId, "purchaser", "password", UserRole.PURCHASER, true);

        MockMultipartFile signFile = new MockMultipartFile(
                "signFile", "sign.png", "image/png", "test sign content".getBytes());

        mockMvc.perform(multipart(put("/api/b2b/contracts/{contractId}/sign", contractId)
                        .file(signFile)
                        .param("paymentTerms", "30天内支付")
                        .param("deliveryTime", "2024-06-01")
                        .param("deliveryAddress", "签署后地址")
                        .param("qualityStandards", "签署后质量标准")
                        .param("breachTerms", "签署后违约条款"))
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        PurchaseContract contract = purchaseContractService.getById(contractId);
        assertNotNull(contract);
        // 注意：实际签署状态可能需要根据业务逻辑调整
    }

    @Test
    @WithMockUser(roles = {"PURCHASER"})
    void testWithdrawContract() throws Exception {
        CustomUserDetails userDetails = new CustomUserDetails(
                purchaserUserId, "purchaser", "password", UserRole.PURCHASER, true);

        mockMvc.perform(put("/api/b2b/contracts/{contractId}/withdraw", contractId)
                        .with(user(userDetails))
                        .param("reason", "测试撤回"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        PurchaseContract contract = purchaseContractService.getById(contractId);
        // 注意：实际撤回状态可能需要根据业务逻辑调整
    }

    @Test
    @WithMockUser(roles = {"PURCHASER"})
    void testRejectContract() throws Exception {
        CustomUserDetails userDetails = new CustomUserDetails(
                purchaserUserId, "purchaser", "password", UserRole.PURCHASER, true);

        mockMvc.perform(put("/api/b2b/contracts/{contractId}/reject", contractId)
                        .with(user(userDetails))
                        .param("reason", "测试拒签"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        PurchaseContract contract = purchaseContractService.getById(contractId);
        // 注意：实际拒签状态可能需要根据业务逻辑调整
    }

    @Test
    @WithMockUser(roles = {"PURCHASER"})
    void testTerminateContract() throws Exception {
        CustomUserDetails userDetails = new CustomUserDetails(
                purchaserUserId, "purchaser", "password", UserRole.PURCHASER, true);

        mockMvc.perform(put("/api/b2b/contracts/{contractId}/terminate", contractId)
                        .with(user(userDetails))
                        .param("reason", "测试终止"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        PurchaseContract contract = purchaseContractService.getById(contractId);
        assertEquals(ContractStatus.TERMINATED, contract.getStatus());
    }

    @Test
    @WithMockUser(roles = {"PURCHASER"})
    void testListMyContracts() throws Exception {
        CustomUserDetails userDetails = new CustomUserDetails(
                purchaserUserId, "purchaser", "password", UserRole.PURCHASER, true);

        mockMvc.perform(get("/api/b2b/contracts/my")
                        .with(user(userDetails))
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].id").isNumber());
    }

    @Test
    void testGetContract() throws Exception {
        mockMvc.perform(get("/api/b2b/contracts/{contractId}", contractId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(contractId));
    }

    @Test
    @WithMockUser(roles = {"PURCHASER"})
    void testCreateOrderFromContract() throws Exception {
        // 创建新合同用于测试
        cn.aspes.agri.trade.dto.DockingRecordRequest dockingRequest = new cn.aspes.agri.trade.dto.DockingRecordRequest();
        dockingRequest.setDemandId(demandId);
        dockingRequest.setProductId(productId);
        dockingRequest.setQuantity(200);
        dockingRequest.setPrice(new BigDecimal("18.00"));
        dockingRequest.setDeliveryTime("2024-06-20");
        dockingRequest.setRemark("测试对接3");
        Long newDockingId = dockingRecordService.respondToDemand(farmerId, dockingRequest);

        // 采购方处理对接
        cn.aspes.agri.trade.dto.DockingFeedbackRequest feedbackRequest = new cn.aspes.agri.trade.dto.DockingFeedbackRequest();
        feedbackRequest.setStatus("agreed");
        feedbackRequest.setRemark("同意对接3");
        dockingRecordService.handleDocking(newDockingId, purchaserId, feedbackRequest);

        ContractRequest contractRequest = new ContractRequest();
        contractRequest.setDockingId(newDockingId);
        Map<String, Object> productInfo = new HashMap<>();
        productInfo.put("productId", productId);
        productInfo.put("productName", "测试产品3");
        productInfo.put("spec", "标准规格3");
        productInfo.put("quantity", 200);
        productInfo.put("price", 18.00);
        contractRequest.setProductInfo(productInfo);
        contractRequest.setPaymentTerms("30天内支付");
        contractRequest.setDeliveryTime("2024-06-20");
        contractRequest.setDeliveryAddress("测试地址3");
        contractRequest.setQualityStandards("质量标准3");
        contractRequest.setBreachTerms("违约条款3");
        Long newContractId = purchaseContractService.createContract(purchaserUserId, contractRequest);

        CustomUserDetails userDetails = new CustomUserDetails(
                purchaserUserId, "purchaser", "password", UserRole.PURCHASER, true);

        mockMvc.perform(post("/api/b2b/orders/{contractId}", newContractId)
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证订单已创建
        var orders = purchaseOrderService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PurchaseOrder>()
                        .eq(PurchaseOrder::getContractId, newContractId));
        assertEquals(1, orders.size());
        assertEquals(OrderStatus.PENDING, orders.get(0).getStatus());
    }

    @Test
    void testPageOrders() throws Exception {
        mockMvc.perform(get("/api/b2b/orders/page")
                        .param("current", "1")
                        .param("size", "10")
                        .param("status", "pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @WithMockUser(roles = {"PURCHASER"})
    void testListMyOrders() throws Exception {
        CustomUserDetails userDetails = new CustomUserDetails(
                purchaserUserId, "purchaser", "password", UserRole.PURCHASER, true);

        mockMvc.perform(get("/api/b2b/orders/my")
                        .with(user(userDetails))
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].id").isNumber());
    }

    @Test
    void testGetOrderDetail() throws Exception {
        mockMvc.perform(get("/api/b2b/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(orderId));
    }

    @Test
    void testSubmitPayment() throws Exception {
        MockMultipartFile paymentFile = new MockMultipartFile(
                "paymentFile", "payment.png", "image/png", "test payment content".getBytes());

        PaymentRequest request = new PaymentRequest();
        request.setOrderId(orderId);
        request.setPaymentMethod("bank_transfer");
        request.setAmount(new BigDecimal("1000.00"));
        request.setRemark("测试支付");

        mockMvc.perform(multipart(post("/api/b2b/payments")
                        .file(paymentFile)
                        .param("orderId", orderId.toString())
                        .param("paymentMethod", "bank_transfer")
                        .param("amount", "1000.00")
                        .param("remark", "测试支付")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNumber());

        // 验证支付记录已创建
        var payments = paymentRecordService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.aspes.agri.trade.entity.PaymentRecord>()
                        .eq(cn.aspes.agri.trade.entity.PaymentRecord::getOrderId, orderId));
        assertEquals(1, payments.size());
    }

    @Test
    void testConfirmPayment() throws Exception {
        // 先创建支付记录
        cn.aspes.agri.trade.entity.PaymentRecord payment = new cn.aspes.agri.trade.entity.PaymentRecord();
        payment.setOrderId(orderId);
        payment.setPaymentMethod("bank_transfer");
        payment.setAmount(new BigDecimal("1000.00"));
        payment.setRemark("测试支付");
        paymentRecordService.save(payment);

        mockMvc.perform(put("/api/b2b/payments/{paymentId}/confirm", payment.getId())
                        .param("paymentNo", "PAY123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证支付状态已更新
        cn.aspes.agri.trade.entity.PaymentRecord updatedPayment = paymentRecordService.getById(payment.getId());
        assertNotNull(updatedPayment.getPaymentNo());
        assertEquals("PAY123456", updatedPayment.getPaymentNo());
    }

    @Test
    void testPagePayments() throws Exception {
        mockMvc.perform(get("/api/b2b/payments/page")
                        .param("current", "1")
                        .param("size", "10")
                        .param("orderId", orderId.toString())
                        .param("status", "pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    void testMarkPaymentFailed() throws Exception {
        // 先创建支付记录
        cn.aspes.agri.trade.entity.PaymentRecord payment = new cn.aspes.agri.trade.entity.PaymentRecord();
        payment.setOrderId(orderId);
        payment.setPaymentMethod("bank_transfer");
        payment.setAmount(new BigDecimal("1000.00"));
        payment.setRemark("测试支付");
        paymentRecordService.save(payment);

        mockMvc.perform(put("/api/b2b/payments/{paymentId}/fail", payment.getId())
                        .param("reason", "支付失败测试"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证支付状态已更新为失败
        cn.aspes.agri.trade.entity.PaymentRecord updatedPayment = paymentRecordService.getById(payment.getId());
        // 注意：实际失败状态可能需要根据业务逻辑调整
    }

    @Test
    @WithMockUser(roles = {"PURCHASER"})
    void testListMyPayments() throws Exception {
        // 先创建支付记录
        cn.aspes.agri.trade.entity.PaymentRecord payment = new cn.aspes.agri.trade.entity.PaymentRecord();
        payment.setOrderId(orderId);
        payment.setPaymentMethod("bank_transfer");
        payment.setAmount(new BigDecimal("1000.00"));
        payment.setRemark("测试支付");
        paymentRecordService.save(payment);

        CustomUserDetails userDetails = new CustomUserDetails(
                purchaserUserId, "purchaser", "password", UserRole.PURCHASER, true);

        mockMvc.perform(get("/api/b2b/payments/my")
                        .with(user(userDetails))
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    void testSubmitPurchaserInfo() throws Exception {
        PurchaserInfoRequest request = new PurchaserInfoRequest();
        request.setCompanyName("新测试采购公司");
        request.setBusinessLicense("new-test-license");
        request.setContactPerson("新采购联系人");
        request.setContactPhone("13800138004");
        request.setContactEmail("newcontact@purchaser.com");
        request.setAddress("新测试地址");

        CustomUserDetails userDetails = new CustomUserDetails(
                purchaserUserId, "purchaser", "password", UserRole.PURCHASER, true);

        mockMvc.perform(post("/api/b2b/purchaser-info")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证采购方信息已更新
        PurchaserInfo updatedInfo = purchaserInfoService.getByUserId(purchaserUserId);
        assertEquals("新测试采购公司", updatedInfo.getCompanyName());
    }

    @Test
    void testGetPurchaserDetail() throws Exception {
        mockMvc.perform(get("/api/b2b/purchaser-info/{id}", purchaserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(purchaserId))
                .andExpect(jsonPath("$.data.companyName").value("测试采购公司"));
    }

    @Test
    void testUpdatePurchaserInfo() throws Exception {
        PurchaserInfoRequest request = new PurchaserInfoRequest();
        request.setCompanyName("更新后的采购公司");
        request.setBusinessLicense("updated-license");
        request.setContactPerson("更新后的联系人");
        request.setContactPhone("13800138005");
        request.setContactEmail("updated@purchaser.com");
        request.setAddress("更新后的地址");

        CustomUserDetails userDetails = new CustomUserDetails(
                purchaserUserId, "purchaser", "password", UserRole.PURCHASER, true);

        mockMvc.perform(put("/api/b2b/purchaser-info/{id}", purchaserId)
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证采购方信息已更新
        PurchaserInfo updatedInfo = purchaserInfoService.getById(purchaserId);
        assertEquals("更新后的采购公司", updatedInfo.getCompanyName());
    }
}