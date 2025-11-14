package cn.aspes.agri.trade.controller.b2c;

import cn.aspes.agri.trade.TestApplication;
import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.dto.FarmerInfoRequest;
import cn.aspes.agri.trade.dto.FarmerProductRequest;
import cn.aspes.agri.trade.dto.LogisticsRequest;
import cn.aspes.agri.trade.dto.LogisticsTraceRequest;
import cn.aspes.agri.trade.entity.FarmerInfo;
import cn.aspes.agri.trade.entity.FarmerProduct;
import cn.aspes.agri.trade.entity.LogisticsRecord;
import cn.aspes.agri.trade.entity.LogisticsTrace;
import cn.aspes.agri.trade.entity.PurchaseOrder;
import cn.aspes.agri.trade.enums.OrderStatus;
import cn.aspes.agri.trade.enums.ProductStatus;
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
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * B2C控制器集成测试
 */
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class B2CControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserService userService;

    @Autowired
    private FarmerInfoService farmerInfoService;

    @Autowired
    private FarmerProductService farmerProductService;

    @Autowired
    private LogisticsRecordService logisticsRecordService;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private PurchaserInfoService purchaserInfoService;

    @Autowired
    private PurchaseDemandService purchaseDemandService;

    @Autowired
    private DockingRecordService dockingRecordService;

    @Autowired
    private PurchaseContractService purchaseContractService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();
    private Long farmerUserId;
    private Long purchaserUserId;
    private Long farmerId;
    private Long purchaserId;
    private Long productId;
    private Long orderId;
    private Long logisticsId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // 创建农户用户
        cn.aspes.agri.trade.dto.UserRegisterRequest farmerRequest = new cn.aspes.agri.trade.dto.UserRegisterRequest();
        farmerRequest.setUsername("farmer" + System.currentTimeMillis());
        farmerRequest.setPassword("password123");
        farmerRequest.setRole(UserRole.FARMER);
        farmerRequest.setContactPerson("农户测试");
        farmerRequest.setContactPhone("13800138001");
        farmerRequest.setContactEmail("farmer@example.com");
        farmerUserId = userService.register(farmerRequest);

        // 创建采购方用户
        cn.aspes.agri.trade.dto.UserRegisterRequest purchaserRequest = new cn.aspes.agri.trade.dto.UserRegisterRequest();
        purchaserRequest.setUsername("purchaser" + System.currentTimeMillis());
        purchaserRequest.setPassword("password123");
        purchaserRequest.setRole(UserRole.PURCHASER);
        purchaserRequest.setContactPerson("采购方测试");
        purchaserRequest.setContactPhone("13800138002");
        purchaserRequest.setContactEmail("purchaser@example.com");
        purchaserUserId = userService.register(purchaserRequest);

        // 创建农户信息
        FarmerInfoRequest farmerInfoRequest = new FarmerInfoRequest();
        farmerInfoRequest.setFarmName("测试农场");
        farmerInfoRequest.setOriginAreaId(1L);
        farmerInfoRequest.setProductionScale("中等规模");
        farmerInfoRequest.setDescription("农场描述");
        farmerInfoService.submitFarmerInfo(farmerUserId, farmerInfoRequest);
        farmerId = farmerInfoService.getByUserId(farmerUserId).getId();

        // 创建采购方信息
        cn.aspes.agri.trade.dto.PurchaserInfoRequest purchaserInfoRequest = new cn.aspes.agri.trade.dto.PurchaserInfoRequest();
        purchaserInfoRequest.setCompanyName("测试采购公司");
        purchaserInfoRequest.setBusinessLicense("test-license");
        purchaserInfoRequest.setContactPerson("采购联系人");
        purchaserInfoRequest.setContactPhone("13800138003");
        purchaserInfoRequest.setContactEmail("contact@purchaser.com");
        purchaserInfoRequest.setAddress("测试地址");
        purchaserInfoService.submitPurchaserInfo(purchaserUserId, purchaserInfoRequest);
        purchaserId = purchaserInfoService.getByUserId(purchaserUserId).getId();

        // 创建农产品
        FarmerProductRequest productRequest = new FarmerProductRequest();
        productRequest.setName("测试产品");
        productRequest.setSpec("标准规格");
        productRequest.setUnit("公斤");
        productRequest.setPrice(new BigDecimal("10.00"));
        productRequest.setMinPurchase(100);
        productRequest.setStock(1000);
        productRequest.setCategoryId(1L);
        productRequest.setOriginAreaId(1L);
        productRequest.setDescription("产品描述");
        productRequest.setQualityStandard("质量标准");
        productRequest.setCertification("有机认证");
        productId = farmerProductService.publishProduct(farmerId, productRequest);

        // 创建采购需求
        cn.aspes.agri.trade.dto.PurchaseDemandRequest demandRequest = new cn.aspes.agri.trade.dto.PurchaseDemandRequest();
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
        Long demandId = purchaseDemandService.publishDemand(purchaserId, demandRequest);

        // 创建对接记录
        cn.aspes.agri.trade.dto.DockingRecordRequest dockingRequest = new cn.aspes.agri.trade.dto.DockingRecordRequest();
        dockingRequest.setDemandId(demandId);
        dockingRequest.setProductId(productId);
        dockingRequest.setQuantity(500);
        dockingRequest.setPrice(new BigDecimal("12.00"));
        dockingRequest.setDeliveryTime("2024-06-01");
        dockingRequest.setRemark("对接备注");
        Long dockingId = dockingRecordService.respondToDemand(farmerId, dockingRequest);

        // 采购方处理对接
        cn.aspes.agri.trade.dto.DockingFeedbackRequest feedbackRequest = new cn.aspes.agri.trade.dto.DockingFeedbackRequest();
        feedbackRequest.setStatus("agreed");
        feedbackRequest.setRemark("同意对接");
        dockingRecordService.handleDocking(dockingId, purchaserId, feedbackRequest);

        // 创建合同
        cn.aspes.agri.trade.dto.ContractRequest contractRequest = new cn.aspes.agri.trade.dto.ContractRequest();
        contractRequest.setDockingId(dockingId);
        java.util.Map<String, Object> productInfo = new java.util.HashMap<>();
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
        Long contractId = purchaseContractService.createContract(purchaserUserId, contractRequest);

        // 创建订单
        orderId = purchaseOrderService.createOrderFromContract(contractId);
    }

    @Test
    @WithMockUser(roles = {"FARMER"})
    void testSubmitFarmerInfo() throws Exception {
        FarmerInfoRequest request = new FarmerInfoRequest();
        request.setFarmName("新测试农场");
        request.setOriginAreaId(2L);
        request.setProductionScale("大规模");
        request.setDescription("新农场描述");

        CustomUserDetails userDetails = new CustomUserDetails(
                farmerUserId, "farmer", "password", UserRole.FARMER, true);

        mockMvc.perform(post("/api/c2c/farmer-info")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证农户信息已更新
        FarmerInfo updatedInfo = farmerInfoService.getByUserId(farmerUserId);
        assertEquals("新测试农场", updatedInfo.getFarmName());
    }

    @Test
    @WithMockUser(roles = {"FARMER"})
    void testUpdateFarmerInfo() throws Exception {
        FarmerInfoRequest request = new FarmerInfoRequest();
        request.setFarmName("更新后的农场");
        request.setOriginAreaId(3L);
        request.setProductionScale("小规模");
        request.setDescription("更新后的描述");

        CustomUserDetails userDetails = new CustomUserDetails(
                farmerUserId, "farmer", "password", UserRole.FARMER, true);

        mockMvc.perform(put("/api/c2c/farmer-info/{id}", farmerId)
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证农户信息已更新
        FarmerInfo updatedInfo = farmerInfoService.getById(farmerId);
        assertEquals("更新后的农场", updatedInfo.getFarmName());
    }

    @Test
    @WithMockUser(roles = {"FARMER"})
    void testGetMyFarmerInfo() throws Exception {
        CustomUserDetails userDetails = new CustomUserDetails(
                farmerUserId, "farmer", "password", UserRole.FARMER, true);

        mockMvc.perform(get("/api/c2c/farmer-info/my")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(farmerId))
                .andExpect(jsonPath("$.data.farmName").value("测试农场"));
    }

    @Test
    void testGetFarmerDetail() throws Exception {
        mockMvc.perform(get("/api/c2c/farmer-info/{id}", farmerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(farmerId))
                .andExpect(jsonPath("$.data.farmName").value("测试农场"));
    }

    @Test
    @WithMockUser(roles = {"FARMER"})
    void testPublishProduct() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "product.jpg", "image/jpeg", "test image content".getBytes());

        FarmerProductRequest request = new FarmerProductRequest();
        request.setName("新产品");
        request.setSpec("新规格");
        request.setUnit("吨");
        request.setPrice(new BigDecimal("5000.00"));
        request.setMinPurchase(10);
        request.setStock(100);
        request.setCategoryId(2L);
        request.setOriginAreaId(2L);
        request.setDescription("新产品描述");
        request.setQualityStandard("新产品质量标准");
        request.setCertification("新产品认证");

        CustomUserDetails userDetails = new CustomUserDetails(
                farmerUserId, "farmer", "password", UserRole.FARMER, true);

        mockMvc.perform(multipart(post("/api/c2c/products")
                        .file(imageFile)
                        .param("name", "新产品")
                        .param("spec", "新规格")
                        .param("unit", "吨")
                        .param("price", "5000.00")
                        .param("minPurchase", "10")
                        .param("stock", "100")
                        .param("categoryId", "2")
                        .param("originAreaId", "2")
                        .param("description", "新产品描述")
                        .param("qualityStandard", "新产品质量标准")
                        .param("certification", "新产品认证"))
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNumber());

        // 验证产品已创建
        var products = farmerProductService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FarmerProduct>()
                        .eq(FarmerProduct::getName, "新产品")
                        .eq(FarmerProduct::getFarmerId, farmerId));
        assertEquals(1, products.size());
    }

    @Test
    @WithMockUser(roles = {"FARMER"})
    void testUpdateProduct() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "updated.jpg", "image/jpeg", "updated image content".getBytes());

        CustomUserDetails userDetails = new CustomUserDetails(
                farmerUserId, "farmer", "password", UserRole.FARMER, true);

        mockMvc.perform(multipart(put("/api/c2c/products/{productId}", productId)
                        .file(imageFile)
                        .param("name", "更新后的产品")
                        .param("spec", "更新后的规格")
                        .param("unit", "箱")
                        .param("price", "15.00")
                        .param("minPurchase", "50")
                        .param("stock", "2000")
                        .param("categoryId", "2")
                        .param("originAreaId", "2")
                        .param("description", "更新后的产品描述")
                        .param("qualityStandard", "更新后的质量标准")
                        .param("certification", "更新后的认证"))
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证产品已更新
        FarmerProduct updatedProduct = farmerProductService.getById(productId);
        assertEquals("更新后的产品", updatedProduct.getName());
        assertEquals("更新后的规格", updatedProduct.getSpec());
    }

    @Test
    @WithMockUser(roles = {"FARMER"})
    void testOnSaleProduct() throws Exception {
        // 先下架产品
        farmerProductService.offSale(productId, farmerId);
        
        CustomUserDetails userDetails = new CustomUserDetails(
                farmerUserId, "farmer", "password", UserRole.FARMER, true);

        mockMvc.perform(put("/api/c2c/products/{productId}/on-sale", productId)
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证产品已上架
        FarmerProduct product = farmerProductService.getById(productId);
        assertEquals(ProductStatus.ON_SALE, product.getStatus());
    }

    @Test
    @WithMockUser(roles = {"FARMER"})
    void testOffSaleProduct() throws Exception {
        CustomUserDetails userDetails = new CustomUserDetails(
                farmerUserId, "farmer", "password", UserRole.FARMER, true);

        mockMvc.perform(put("/api/c2c/products/{productId}/off-sale", productId)
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证产品已下架
        FarmerProduct product = farmerProductService.getById(productId);
        assertEquals(ProductStatus.OFF_SALE, product.getStatus());
    }

    @Test
    void testListProducts() throws Exception {
        mockMvc.perform(get("/api/c2c/products")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("categoryId", "1")
                        .param("originAreaId", "1")
                        .param("status", "on_sale"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].name").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = {"FARMER"})
    void testListMyProducts() throws Exception {
        CustomUserDetails userDetails = new CustomUserDetails(
                farmerUserId, "farmer", "password", UserRole.FARMER, true);

        mockMvc.perform(get("/api/c2c/products/my")
                        .with(user(userDetails))
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].name").isNotEmpty());
    }

    @Test
    void testGetProduct() throws Exception {
        mockMvc.perform(get("/api/c2c/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(productId))
                .andExpect(jsonPath("$.data.name").value("测试产品"));
    }

    @Test
    void testCreateLogistics() throws Exception {
        LogisticsRequest request = new LogisticsRequest();
        request.setOrderId(orderId);
        request.setLogisticsCompany("测试物流公司");
        request.setLogisticsNo("TEST123456");
        request.setSenderName("发件人");
        request.setSenderPhone("13800138001");
        request.setSenderAddress("发件地址");
        request.setReceiverName("收件人");
        request.setReceiverPhone("13800138002");
        request.setReceiverAddress("收件地址");

        mockMvc.perform(post("/api/c2c/logistics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNumber());

        // 验证物流记录已创建
        var logisticsRecords = logisticsRecordService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<LogisticsRecord>()
                        .eq(LogisticsRecord::getOrderId, orderId));
        assertEquals(1, logisticsRecords.size());
        logisticsId = logisticsRecords.get(0).getId();
    }

    @Test
    void testShipGoods() throws Exception {
        // 先创建物流记录
        LogisticsRequest request = new LogisticsRequest();
        request.setOrderId(orderId);
        request.setLogisticsCompany("测试物流公司");
        request.setLogisticsNo("TEST123456");
        request.setSenderName("发件人");
        request.setSenderPhone("13800138001");
        request.setSenderAddress("发件地址");
        request.setReceiverName("收件人");
        request.setReceiverPhone("13800138002");
        request.setReceiverAddress("收件地址");
        logisticsId = logisticsRecordService.createLogistics(request);

        mockMvc.perform(put("/api/c2c/logistics/{logisticsId}/ship", logisticsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证物流状态已更新
        LogisticsRecord logistics = logisticsRecordService.getById(logisticsId);
        // 注意：实际发货状态可能需要根据业务逻辑调整
    }

    @Test
    void testAddTrace() throws Exception {
        // 先创建物流记录
        LogisticsRequest request = new LogisticsRequest();
        request.setOrderId(orderId);
        request.setLogisticsCompany("测试物流公司");
        request.setLogisticsNo("TEST123456");
        request.setSenderName("发件人");
        request.setSenderPhone("13800138001");
        request.setSenderAddress("发件地址");
        request.setReceiverName("收件人");
        request.setReceiverPhone("13800138002");
        request.setReceiverAddress("收件地址");
        logisticsId = logisticsRecordService.createLogistics(request);

        LogisticsTraceRequest traceRequest = new LogisticsTraceRequest();
        traceRequest.setTraceTime(new Date());
        traceRequest.setTraceLocation("测试地点");
        traceRequest.setTraceInfo("测试轨迹信息");

        mockMvc.perform(post("/api/c2c/logistics/{logisticsId}/trace", logisticsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(traceRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证物流轨迹已添加
        var traces = logisticsRecordService.listTraces(logisticsId);
        assertEquals(1, traces.size());
    }

    @Test
    void testConfirmReceipt() throws Exception {
        // 先创建物流记录
        LogisticsRequest request = new LogisticsRequest();
        request.setOrderId(orderId);
        request.setLogisticsCompany("测试物流公司");
        request.setLogisticsNo("TEST123456");
        request.setSenderName("发件人");
        request.setSenderPhone("13800138001");
        request.setSenderAddress("发件地址");
        request.setReceiverName("收件人");
        request.setReceiverPhone("13800138002");
        request.setReceiverAddress("收件地址");
        logisticsId = logisticsRecordService.createLogistics(request);

        mockMvc.perform(put("/api/c2c/logistics/{logisticsId}/confirm", logisticsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证物流状态已更新
        LogisticsRecord logistics = logisticsRecordService.getById(logisticsId);
        // 注意：实际签收状态可能需要根据业务逻辑调整
    }

    @Test
    void testGetLogisticsByOrderId() throws Exception {
        // 先创建物流记录
        LogisticsRequest request = new LogisticsRequest();
        request.setOrderId(orderId);
        request.setLogisticsCompany("测试物流公司");
        request.setLogisticsNo("TEST123456");
        request.setSenderName("发件人");
        request.setSenderPhone("13800138001");
        request.setSenderAddress("发件地址");
        request.setReceiverName("收件人");
        request.setReceiverPhone("13800138002");
        request.setReceiverAddress("收件地址");
        logisticsId = logisticsRecordService.createLogistics(request);

        mockMvc.perform(get("/api/c2c/logistics/order/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(logisticsId))
                .andExpect(jsonPath("$.data.logisticsCompany").value("测试物流公司"));
    }

    @Test
    void testListTraces() throws Exception {
        // 先创建物流记录
        LogisticsRequest request = new LogisticsRequest();
        request.setOrderId(orderId);
        request.setLogisticsCompany("测试物流公司");
        request.setLogisticsNo("TEST123456");
        request.setSenderName("发件人");
        request.setSenderPhone("13800138001");
        request.setSenderAddress("发件地址");
        request.setReceiverName("收件人");
        request.setReceiverPhone("13800138002");
        request.setReceiverAddress("收件地址");
        logisticsId = logisticsRecordService.createLogistics(request);

        // 添加轨迹
        LogisticsTraceRequest traceRequest = new LogisticsTraceRequest();
        traceRequest.setTraceTime(new Date());
        traceRequest.setTraceLocation("测试地点");
        traceRequest.setTraceInfo("测试轨迹信息");
        logisticsRecordService.addTrace(logisticsId, traceRequest);

        mockMvc.perform(get("/api/c2c/logistics/{logisticsId}/traces", logisticsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].traceInfo").value("测试轨迹信息"));
    }

    @Test
    void testPageTraces() throws Exception {
        // 先创建物流记录
        LogisticsRequest request = new LogisticsRequest();
        request.setOrderId(orderId);
        request.setLogisticsCompany("测试物流公司");
        request.setLogisticsNo("TEST123456");
        request.setSenderName("发件人");
        request.setSenderPhone("13800138001");
        request.setSenderAddress("发件地址");
        request.setReceiverName("收件人");
        request.setReceiverPhone("13800138002");
        request.setReceiverAddress("收件地址");
        logisticsId = logisticsRecordService.createLogistics(request);

        // 添加多条轨迹
        for (int i = 0; i < 5; i++) {
            LogisticsTraceRequest traceRequest = new LogisticsTraceRequest();
            traceRequest.setTraceTime(new Date());
            traceRequest.setTraceLocation("测试地点" + i);
            traceRequest.setTraceInfo("测试轨迹信息" + i);
            logisticsRecordService.addTrace(logisticsId, traceRequest);
        }

        mockMvc.perform(get("/api/c2c/logistics/{logisticsId}/traces-page", logisticsId)
                        .param("current", "1")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.length").value(3));
    }
}