package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.dto.LogisticsRequest;
import cn.aspes.agri.trade.dto.LogisticsTraceRequest;
import cn.aspes.agri.trade.dto.UserRegisterRequest;
import cn.aspes.agri.trade.entity.*;
import cn.aspes.agri.trade.enums.ContractStatus;
import cn.aspes.agri.trade.enums.LogisticsStatus;
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
 * 物流轨迹服务集成测试
 */
@SpringBootTest(classes = cn.aspes.agri.trade.AgriTradePlatformApplication.class)
@ActiveProfiles("test")
@Transactional
class LogisticsTraceServiceIntegrationTest {

    @Autowired
    private LogisticsTraceService logisticsTraceService;

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
    private Long logisticsId;

    @BeforeEach
    void setUp() {
        // 创建农民用户
        UserRegisterRequest farmerRequest = new UserRegisterRequest();
        farmerRequest.setUsername("farmer_trace" + System.currentTimeMillis());
        farmerRequest.setPassword("password123");
        farmerRequest.setRole(UserRole.FARMER);
        farmerRequest.setContactPerson("张三");
        farmerRequest.setContactPhone("13800138000");
        farmerRequest.setContactEmail("farmer@example.com");
        farmerId = userService.register(farmerRequest);

        // 创建采购方用户
        UserRegisterRequest purchaserRequest = new UserRegisterRequest();
        purchaserRequest.setUsername("purchaser_trace" + System.currentTimeMillis());
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

        // 创建物流记录
        LogisticsRequest request = new LogisticsRequest();
        request.setOrderId(orderId);
        request.setLogisticsCompany("测试快递");
        request.setTrackingNo("TEST123456789");
        request.setTransportType("快递");
        logisticsId = logisticsRecordService.createLogistics(request);
    }

    @Test
    void testListByLogisticsId() {
        // 添加物流轨迹
        LogisticsTraceRequest trace1 = new LogisticsTraceRequest();
        trace1.setNodeTime(LocalDateTime.now().minusHours(2));
        trace1.setNodeLocation("发货地");
        trace1.setNodeDesc("已揽收");

        LogisticsTraceRequest trace2 = new LogisticsTraceRequest();
        trace2.setNodeTime(LocalDateTime.now().minusHours(1));
        trace2.setNodeLocation("中转站");
        trace2.setNodeDesc("运输中");

        LogisticsTraceRequest trace3 = new LogisticsTraceRequest();
        trace3.setNodeTime(LocalDateTime.now());
        trace3.setNodeLocation("目的地");
        trace3.setNodeDesc("派送中");

        logisticsRecordService.addTrace(logisticsId, trace1);
        logisticsRecordService.addTrace(logisticsId, trace2);
        logisticsRecordService.addTrace(logisticsId, trace3);

        // 使用LogisticsTraceService查询轨迹
        List<LogisticsTrace> traces = logisticsTraceService.listByLogisticsId(logisticsId);
        assertEquals(3, traces.size());
        
        // 验证轨迹按时间顺序排列
        assertTrue(traces.get(0).getNodeTime().isBefore(traces.get(1).getNodeTime()));
        assertTrue(traces.get(1).getNodeTime().isBefore(traces.get(2).getNodeTime()));
        
        // 验证轨迹内容
        assertEquals("发货地", traces.get(0).getNodeLocation());
        assertEquals("中转站", traces.get(1).getNodeLocation());
        assertEquals("目的地", traces.get(2).getNodeLocation());
    }

    @Test
    void testListByLogisticsIdEmpty() {
        // 查询不存在的物流ID的轨迹
        List<LogisticsTrace> traces = logisticsTraceService.listByLogisticsId(99999L);
        assertTrue(traces.isEmpty());
    }

    @Test
    void testListByLogisticsIdWithMultipleTraces() {
        // 添加多个物流轨迹
        for (int i = 0; i < 10; i++) {
            LogisticsTraceRequest trace = new LogisticsTraceRequest();
            trace.setNodeTime(LocalDateTime.now().minusMinutes(30 - i * 3));
            trace.setNodeLocation("节点" + i);
            trace.setNodeDesc("轨迹信息" + i);

            logisticsRecordService.addTrace(logisticsId, trace);
        }

        // 查询轨迹
        List<LogisticsTrace> traces = logisticsTraceService.listByLogisticsId(logisticsId);
        assertEquals(10, traces.size());
        
        // 验证所有轨迹都按时间顺序排列
        for (int i = 0; i < traces.size() - 1; i++) {
            assertTrue(traces.get(i).getNodeTime().isBefore(traces.get(i + 1).getNodeTime()));
            assertEquals("节点" + i, traces.get(i).getNodeLocation());
            assertEquals("轨迹信息" + i, traces.get(i).getNodeDesc());
        }
    }

    @Test
    void testListByLogisticsIdWithSameTime() {
        // 添加相同时间的物流轨迹
        LocalDateTime sameTime = LocalDateTime.now();
        
        LogisticsTraceRequest trace1 = new LogisticsTraceRequest();
        trace1.setNodeTime(sameTime);
        trace1.setNodeLocation("位置1");
        trace1.setNodeDesc("描述1");

        LogisticsTraceRequest trace2 = new LogisticsTraceRequest();
        trace2.setNodeTime(sameTime);
        trace2.setNodeLocation("位置2");
        trace2.setNodeDesc("描述2");

        logisticsRecordService.addTrace(logisticsId, trace1);
        logisticsRecordService.addTrace(logisticsId, trace2);

        // 查询轨迹
        List<LogisticsTrace> traces = logisticsTraceService.listByLogisticsId(logisticsId);
        assertEquals(2, traces.size());
        
        // 验证两个轨迹的时间相同
        assertEquals(sameTime, traces.get(0).getNodeTime());
        assertEquals(sameTime, traces.get(1).getNodeTime());
    }

    @Test
    void testListByLogisticsIdWithLogisticsLifecycle() {
        // 创建物流记录
        LogisticsRequest request = new LogisticsRequest();
        request.setOrderId(orderId);
        request.setLogisticsCompany("生命周期测试快递");
        request.setTrackingNo("LIFECYCLE123");
        request.setTransportType("快递");

        Long newLogisticsId = logisticsRecordService.createLogistics(request);

        // 物流生命周期中的轨迹
        // 1. 创建物流记录时，无轨迹
        List<LogisticsTrace> traces = logisticsTraceService.listByLogisticsId(newLogisticsId);
        assertTrue(traces.isEmpty());

        // 2. 发货时，添加发货轨迹
        logisticsRecordService.shipGoods(newLogisticsId);
        traces = logisticsTraceService.listByLogisticsId(newLogisticsId);
        assertEquals(1, traces.size());
        assertEquals("已发货", traces.get(0).getNodeDesc());

        // 3. 添加运输轨迹
        LogisticsTraceRequest transportTrace = new LogisticsTraceRequest();
        transportTrace.setNodeTime(LocalDateTime.now());
        transportTrace.setNodeLocation("运输中转站");
        transportTrace.setNodeDesc("货物运输中");
        logisticsRecordService.addTrace(newLogisticsId, transportTrace);

        // 4. 确认签收时，添加签收轨迹
        logisticsRecordService.confirmReceipt(newLogisticsId);
        traces = logisticsTraceService.listByLogisticsId(newLogisticsId);
        
        // 验证有3个轨迹：发货、运输、签收
        assertEquals(3, traces.size());
        assertEquals("已发货", traces.get(0).getNodeDesc());
        assertEquals("货物运输中", traces.get(1).getNodeDesc());
        assertEquals("已签收", traces.get(2).getNodeDesc());
    }
}