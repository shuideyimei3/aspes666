package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.dto.LogisticsRequest;
import cn.aspes.agri.trade.dto.LogisticsTraceRequest;
import cn.aspes.agri.trade.dto.UserRegisterRequest;
import cn.aspes.agri.trade.entity.*;
import cn.aspes.agri.trade.enums.ContractStatus;
import cn.aspes.agri.trade.enums.LogisticsStatus;
import cn.aspes.agri.trade.enums.OrderStatus;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 物流服务集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LogisticsIntegrationTest {

    @Autowired
    private LogisticsRecordService logisticsRecordService;

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
        farmerRequest.setUsername("farmer_log" + System.currentTimeMillis());
        farmerRequest.setPassword("password123");
        farmerRequest.setRole(UserRole.FARMER);
        farmerRequest.setContactPerson("张三");
        farmerRequest.setContactPhone("13800138000");
        farmerRequest.setContactEmail("farmer@example.com");
        farmerId = userService.register(farmerRequest);

        // 创建采购方用户
        UserRegisterRequest purchaserRequest = new UserRegisterRequest();
        purchaserRequest.setUsername("purchaser_log" + System.currentTimeMillis());
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
    void testCreateLogistics() {
        LogisticsRequest request = new LogisticsRequest();
        request.setOrderId(orderId);
        request.setLogisticsCompany("顺丰快递");
        request.setTrackingNo("SF1234567890");
        request.setTransportType("快递");

        Long logisticsId = logisticsRecordService.createLogistics(request);
        assertNotNull(logisticsId);

        LogisticsRecord logistics = logisticsRecordService.getById(logisticsId);
        assertNotNull(logistics);
        assertEquals(orderId, logistics.getOrderId());
        assertEquals("顺丰快递", logistics.getLogisticsCompany());
        assertEquals("SF1234567890", logistics.getTrackingNo());
        assertEquals(LogisticsStatus.PENDING, logistics.getStatus());
    }

    @Test
    void testShipGoods() {
        // 创建物流记录
        LogisticsRequest request = new LogisticsRequest();
        request.setOrderId(orderId);
        request.setLogisticsCompany("圆通快递");
        request.setTrackingNo("YT9876543210");
        request.setTransportType("快递");

        Long logisticsId = logisticsRecordService.createLogistics(request);

        // 发货
        logisticsRecordService.shipGoods(logisticsId);

        LogisticsRecord logistics = logisticsRecordService.getById(logisticsId);
        assertEquals(LogisticsStatus.SHIPPED, logistics.getStatus());
        assertNotNull(logistics.getDepartureTime());
    }

    @Test
    void testAddTrace() {
        // 创建物流记录
        LogisticsRequest request = new LogisticsRequest();
        request.setOrderId(orderId);
        request.setLogisticsCompany("中通快递");
        request.setTrackingNo("ZT5555666666");
        request.setTransportType("快递");

        Long logisticsId = logisticsRecordService.createLogistics(request);
        logisticsRecordService.shipGoods(logisticsId);

        // 添加物流轨迹
        LogisticsTraceRequest trace1 = new LogisticsTraceRequest();
        trace1.setNodeTime(LocalDateTime.now());
        trace1.setNodeLocation("杭州分拨中心");
        trace1.setNodeDesc("商品在杭州分拨中心，进行中转");

        logisticsRecordService.addTrace(logisticsId, trace1);

        List<LogisticsTrace> traces = logisticsRecordService.listTraces(logisticsId);
        assertTrue(traces.size() > 0);
    }

    @Test
    void testConfirmReceipt() {
        // 创建物流记录
        LogisticsRequest request = new LogisticsRequest();
        request.setOrderId(orderId);
        request.setLogisticsCompany("申通快递");
        request.setTrackingNo("ST1111222222");
        request.setTransportType("快递");

        Long logisticsId = logisticsRecordService.createLogistics(request);
        logisticsRecordService.shipGoods(logisticsId);

        // 添加物流轨迹
        LogisticsTraceRequest trace = new LogisticsTraceRequest();
        trace.setNodeTime(LocalDateTime.now());
        trace.setNodeLocation("目的地收货处");
        trace.setNodeDesc("商品已到达，请及时签收");

        logisticsRecordService.addTrace(logisticsId, trace);

        // 确认签收
        logisticsRecordService.confirmReceipt(logisticsId);

        LogisticsRecord logistics = logisticsRecordService.getById(logisticsId);
        assertEquals(LogisticsStatus.SIGNED, logistics.getStatus());
        assertNotNull(logistics.getArrivalTime());
    }

    @Test
    void testGetByOrderId() {
        // 创建物流记录
        LogisticsRequest request = new LogisticsRequest();
        request.setOrderId(orderId);
        request.setLogisticsCompany("韵达快递");
        request.setTrackingNo("YD3333444444");
        request.setTransportType("快递");

        logisticsRecordService.createLogistics(request);

        // 根据订单ID查询物流信息
        LogisticsRecord logistics = logisticsRecordService.getByOrderId(orderId);
        assertNotNull(logistics);
        assertEquals(orderId, logistics.getOrderId());
        assertEquals("韵达快递", logistics.getLogisticsCompany());
    }

    @Test
    void testListTraces() {
        // 创建物流记录
        LogisticsRequest request = new LogisticsRequest();
        request.setOrderId(orderId);
        request.setLogisticsCompany("优速快递");
        request.setTrackingNo("US7777888888");
        request.setTransportType("快递");

        Long logisticsId = logisticsRecordService.createLogistics(request);
        logisticsRecordService.shipGoods(logisticsId);

        // 添加多个物流轨迹
        for (int i = 0; i < 3; i++) {
            LogisticsTraceRequest trace = new LogisticsTraceRequest();
            trace.setNodeTime(LocalDateTime.now().plusMinutes(i));
            trace.setNodeLocation("节点" + i);
            trace.setNodeDesc("轨迹信息" + i);

            logisticsRecordService.addTrace(logisticsId, trace);
        }

        List<LogisticsTrace> traces = logisticsRecordService.listTraces(logisticsId);
        assertTrue(traces.size() >= 3);
    }

    @Test
    void testPageTraces() {
        // 创建物流记录
        LogisticsRequest request = new LogisticsRequest();
        request.setOrderId(orderId);
        request.setLogisticsCompany("邮政快递");
        request.setTrackingNo("YZ9999000000");
        request.setTransportType("快递");

        Long logisticsId = logisticsRecordService.createLogistics(request);
        logisticsRecordService.shipGoods(logisticsId);

        // 添加物流轨迹
        for (int i = 0; i < 5; i++) {
            LogisticsTraceRequest trace = new LogisticsTraceRequest();
            trace.setNodeTime(LocalDateTime.now().plusHours(i));
            trace.setNodeLocation("分拨" + i);
            trace.setNodeDesc("分拨信息" + i);

            logisticsRecordService.addTrace(logisticsId, trace);
        }

        // 分页查询物流轨迹
        Page<LogisticsTrace> page = logisticsRecordService.pageTraces(logisticsId, 1, 10);
        assertTrue(page.getRecords().size() >= 5);
    }

    @Test
    void testLogisticsLifecycle() {
        // 创建物流记录
        LogisticsRequest request = new LogisticsRequest();
        request.setOrderId(orderId);
        request.setLogisticsCompany("顺丰快递");
        request.setTrackingNo("SF1111111111");
        request.setTransportType("快递");

        Long logisticsId = logisticsRecordService.createLogistics(request);

        // 验证初始状态
        LogisticsRecord logistics = logisticsRecordService.getById(logisticsId);
        assertEquals(LogisticsStatus.PENDING, logistics.getStatus());

        // 发货
        logisticsRecordService.shipGoods(logisticsId);
        logistics = logisticsRecordService.getById(logisticsId);
        assertEquals(LogisticsStatus.SHIPPED, logistics.getStatus());

        // 添加运输轨迹
        LogisticsTraceRequest trace = new LogisticsTraceRequest();
        trace.setNodeTime(LocalDateTime.now());
        trace.setNodeLocation("运输途中");
        trace.setNodeDesc("商品已发货，在运输途中");

        logisticsRecordService.addTrace(logisticsId, trace);

        // 确认签收
        logisticsRecordService.confirmReceipt(logisticsId);
        logistics = logisticsRecordService.getById(logisticsId);
        assertEquals(LogisticsStatus.SIGNED, logistics.getStatus());
    }

    @Test
    void testMultipleLogisticsCompanies() {
        // 测试不同物流公司
        String[] companies = {"顺丰快递", "圆通快递", "中通快递", "申通快递"};

        for (int i = 0; i < companies.length; i++) {
            LogisticsRequest request = new LogisticsRequest();
            request.setOrderId(orderId);
            request.setLogisticsCompany(companies[i]);
            request.setTrackingNo("TRACK" + i + System.currentTimeMillis());
            request.setTransportType("快递");

            Long logisticsId = logisticsRecordService.createLogistics(request);
            assertNotNull(logisticsId);

            LogisticsRecord logistics = logisticsRecordService.getById(logisticsId);
            assertEquals(companies[i], logistics.getLogisticsCompany());
        }
    }
}
