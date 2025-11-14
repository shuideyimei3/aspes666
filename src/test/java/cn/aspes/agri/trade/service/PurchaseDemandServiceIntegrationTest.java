package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.dto.PurchaseDemandRequest;
import cn.aspes.agri.trade.entity.PurchaseDemand;
import cn.aspes.agri.trade.entity.PurchaserInfo;
import cn.aspes.agri.trade.enums.DemandStatus;
import cn.aspes.agri.trade.enums.UserRole;
import cn.aspes.agri.trade.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
 * 采购需求管理服务集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PurchaseDemandServiceIntegrationTest {

    @Autowired
    private PurchaseDemandService purchaseDemandService;
    
    @Autowired
    private PurchaserInfoService purchaserInfoService;
    
    @Autowired
    private UserService userService;
    
    private Long purchaserId;
    private Long otherPurchaserId;

    @BeforeEach
    void setUp() {
        // 创建采购方用户
        cn.aspes.agri.trade.dto.UserRegisterRequest purchaserRequest = new cn.aspes.agri.trade.dto.UserRegisterRequest();
        purchaserRequest.setUsername("purchaser_demand" + System.currentTimeMillis());
        purchaserRequest.setPassword("password123");
        purchaserRequest.setRole(UserRole.PURCHASER);
        purchaserRequest.setContactPerson("采购方需求测试");
        purchaserRequest.setContactPhone("13800138001");
        purchaserRequest.setContactEmail("purchaser_demand@example.com");
        purchaserId = userService.register(purchaserRequest);
        
        // 创建另一个采购方用户
        cn.aspes.agri.trade.dto.UserRegisterRequest otherPurchaserRequest = new cn.aspes.agri.trade.dto.UserRegisterRequest();
        otherPurchaserRequest.setUsername("other_purchaser" + System.currentTimeMillis());
        otherPurchaserRequest.setPassword("password123");
        otherPurchaserRequest.setRole(UserRole.PURCHASER);
        otherPurchaserRequest.setContactPerson("其他采购方");
        otherPurchaserRequest.setContactPhone("13800138002");
        otherPurchaserRequest.setContactEmail("other_purchaser@example.com");
        otherPurchaserId = userService.register(otherPurchaserRequest);
        
        // 创建采购方信息
        PurchaserInfo purchaserInfo = new PurchaserInfo();
        purchaserInfo.setUserId(purchaserId);
        purchaserInfo.setCompanyName("测试采购公司");
        purchaserInfo.setBusinessLicense("http://example.com/license");
        purchaserInfoService.save(purchaserInfo);
        
        PurchaserInfo otherPurchaserInfo = new PurchaserInfo();
        otherPurchaserInfo.setUserId(otherPurchaserId);
        otherPurchaserInfo.setCompanyName("其他采购公司");
        otherPurchaserInfo.setBusinessLicense("http://example.com/license2");
        purchaserInfoService.save(otherPurchaserInfo);
    }

    @Test
    @DisplayName("采购方发布需求")
    void testPublishDemand() {
        PurchaseDemandRequest request = new PurchaseDemandRequest();
        request.setTitle("采购优质水稻");
        request.setCategoryId(1L);
        request.setSpec("一级大米");
        request.setUnit("公斤");
        request.setQuantity(1000);
        request.setPriceRange("5.00-6.00");
        request.setDeliveryTime(LocalDate.now().plusDays(30));
        request.setDeliveryAddress("浙江省杭州市");
        request.setQualityStandard("符合国家一级大米标准");
        request.setRemark("需要有机认证");
        request.setDeadline(LocalDate.now().plusDays(15));

        Long demandId = purchaseDemandService.publishDemand(purchaserId, request);
        
        assertNotNull(demandId);
        assertTrue(demandId > 0);
        
        // 验证需求信息
        PurchaseDemand demand = purchaseDemandService.getById(demandId);
        assertNotNull(demand);
        assertEquals(purchaserId, demand.getPurchaserId());
        assertEquals("采购优质水稻", demand.getTitle());
        assertEquals(Long.valueOf(1L), demand.getCategoryId());
        assertEquals("一级大米", demand.getSpec());
        assertEquals("公斤", demand.getUnit());
        assertEquals(Integer.valueOf(1000), demand.getQuantity());
        assertEquals("5.00-6.00", demand.getPriceRange());
        assertEquals(LocalDate.now().plusDays(30), demand.getDeliveryTime());
        assertEquals("浙江省杭州市", demand.getDeliveryAddress());
        assertEquals("符合国家一级大米标准", demand.getQualityStandard());
        assertEquals("需要有机认证", demand.getRemark());
        assertEquals(LocalDate.now().plusDays(15), demand.getDeadline());
        assertEquals(DemandStatus.PENDING, demand.getStatus());
    }

    @Test
    @DisplayName("不存在的采购方发布需求应失败")
    void testPublishDemandByNonExistentPurchaser() {
        PurchaseDemandRequest request = new PurchaseDemandRequest();
        request.setTitle("测试需求");
        request.setCategoryId(1L);
        request.setSpec("规格");
        request.setUnit("公斤");
        request.setQuantity(100);
        request.setPriceRange("5.00-6.00");
        request.setDeliveryTime(LocalDate.now().plusDays(30));
        request.setDeliveryAddress("测试地址");
        request.setQualityStandard("标准");
        request.setDeadline(LocalDate.now().plusDays(15));

        assertThrows(BusinessException.class, () -> {
            purchaseDemandService.publishDemand(999999L, request);
        });
    }

    @Test
    @DisplayName("采购方更新需求")
    void testUpdateDemand() {
        // 先发布需求
        PurchaseDemandRequest publishRequest = new PurchaseDemandRequest();
        publishRequest.setTitle("原始需求");
        publishRequest.setCategoryId(1L);
        publishRequest.setSpec("原始规格");
        publishRequest.setUnit("公斤");
        publishRequest.setQuantity(100);
        publishRequest.setPriceRange("5.00-6.00");
        publishRequest.setDeliveryTime(LocalDate.now().plusDays(30));
        publishRequest.setDeliveryAddress("原始地址");
        publishRequest.setQualityStandard("原始标准");
        publishRequest.setDeadline(LocalDate.now().plusDays(15));
        
        Long demandId = purchaseDemandService.publishDemand(purchaserId, publishRequest);
        
        // 更新需求
        PurchaseDemandRequest updateRequest = new PurchaseDemandRequest();
        updateRequest.setTitle("更新后的需求");
        updateRequest.setCategoryId(2L);
        updateRequest.setSpec("更新后的规格");
        updateRequest.setUnit("袋");
        updateRequest.setQuantity(200);
        updateRequest.setPriceRange("6.00-7.00");
        updateRequest.setDeliveryTime(LocalDate.now().plusDays(45));
        updateRequest.setDeliveryAddress("更新后的地址");
        updateRequest.setQualityStandard("更新后的标准");
        updateRequest.setRemark("更新后的备注");
        updateRequest.setDeadline(LocalDate.now().plusDays(30));
        
        purchaseDemandService.updateDemand(demandId, purchaserId, updateRequest);
        
        // 验证更新结果
        PurchaseDemand demand = purchaseDemandService.getById(demandId);
        assertEquals("更新后的需求", demand.getTitle());
        assertEquals(Long.valueOf(2L), demand.getCategoryId());
        assertEquals("更新后的规格", demand.getSpec());
        assertEquals("袋", demand.getUnit());
        assertEquals(Integer.valueOf(200), demand.getQuantity());
        assertEquals("6.00-7.00", demand.getPriceRange());
        assertEquals(LocalDate.now().plusDays(45), demand.getDeliveryTime());
        assertEquals("更新后的地址", demand.getDeliveryAddress());
        assertEquals("更新后的标准", demand.getQualityStandard());
        assertEquals("更新后的备注", demand.getRemark());
        assertEquals(LocalDate.now().plusDays(30), demand.getDeadline());
    }

    @Test
    @DisplayName("非需求所有者更新需求应失败")
    void testUpdateDemandByNonOwner() {
        // 先发布需求
        PurchaseDemandRequest publishRequest = new PurchaseDemandRequest();
        publishRequest.setTitle("测试需求");
        publishRequest.setCategoryId(1L);
        publishRequest.setSpec("规格");
        publishRequest.setUnit("公斤");
        publishRequest.setQuantity(100);
        publishRequest.setPriceRange("5.00-6.00");
        publishRequest.setDeliveryTime(LocalDate.now().plusDays(30));
        publishRequest.setDeliveryAddress("测试地址");
        publishRequest.setQualityStandard("标准");
        publishRequest.setDeadline(LocalDate.now().plusDays(15));
        
        Long demandId = purchaseDemandService.publishDemand(purchaserId, publishRequest);
        
        // 尝试用其他采购方更新需求
        PurchaseDemandRequest updateRequest = new PurchaseDemandRequest();
        updateRequest.setTitle("被篡改的需求");
        updateRequest.setCategoryId(2L);
        updateRequest.setSpec("规格");
        updateRequest.setUnit("公斤");
        updateRequest.setQuantity(50);
        updateRequest.setPriceRange("1.00-2.00");
        
        assertThrows(BusinessException.class, () -> {
            purchaseDemandService.updateDemand(demandId, otherPurchaserId, updateRequest);
        });
    }

    @Test
    @DisplayName("更新非待匹配状态的需求应失败")
    void testUpdateNonPendingDemand() {
        // 先发布需求
        PurchaseDemandRequest publishRequest = new PurchaseDemandRequest();
        publishRequest.setTitle("测试需求");
        publishRequest.setCategoryId(1L);
        publishRequest.setSpec("规格");
        publishRequest.setUnit("公斤");
        publishRequest.setQuantity(100);
        publishRequest.setPriceRange("5.00-6.00");
        publishRequest.setDeliveryTime(LocalDate.now().plusDays(30));
        publishRequest.setDeliveryAddress("测试地址");
        publishRequest.setQualityStandard("标准");
        publishRequest.setDeadline(LocalDate.now().plusDays(15));
        
        Long demandId = purchaseDemandService.publishDemand(purchaserId, publishRequest);
        
        // 手动修改需求状态为已匹配
        PurchaseDemand demand = purchaseDemandService.getById(demandId);
        demand.setStatus(DemandStatus.MATCHED);
        purchaseDemandService.updateById(demand);
        
        // 尝试更新已匹配的需求
        PurchaseDemandRequest updateRequest = new PurchaseDemandRequest();
        updateRequest.setTitle("更新后的需求");
        updateRequest.setCategoryId(2L);
        updateRequest.setSpec("更新后的规格");
        
        assertThrows(BusinessException.class, () -> {
            purchaseDemandService.updateDemand(demandId, purchaserId, updateRequest);
        });
    }

    @Test
    @DisplayName("采购方关闭需求")
    void testCloseDemand() {
        // 先发布需求
        PurchaseDemandRequest request = new PurchaseDemandRequest();
        request.setTitle("关闭测试需求");
        request.setCategoryId(1L);
        request.setSpec("规格");
        request.setUnit("公斤");
        request.setQuantity(100);
        request.setPriceRange("5.00-6.00");
        request.setDeliveryTime(LocalDate.now().plusDays(30));
        request.setDeliveryAddress("测试地址");
        request.setQualityStandard("标准");
        request.setDeadline(LocalDate.now().plusDays(15));
        
        Long demandId = purchaseDemandService.publishDemand(purchaserId, request);
        
        // 关闭需求
        purchaseDemandService.closeDemand(demandId, purchaserId);
        
        // 验证需求状态
        PurchaseDemand demand = purchaseDemandService.getById(demandId);
        assertEquals(DemandStatus.CLOSED, demand.getStatus());
    }

    @Test
    @DisplayName("非需求所有者关闭需求应失败")
    void testCloseDemandByNonOwner() {
        // 先发布需求
        PurchaseDemandRequest request = new PurchaseDemandRequest();
        request.setTitle("关闭测试需求");
        request.setCategoryId(1L);
        request.setSpec("规格");
        request.setUnit("公斤");
        request.setQuantity(100);
        request.setPriceRange("5.00-6.00");
        request.setDeliveryTime(LocalDate.now().plusDays(30));
        request.setDeliveryAddress("测试地址");
        request.setQualityStandard("标准");
        request.setDeadline(LocalDate.now().plusDays(15));
        
        Long demandId = purchaseDemandService.publishDemand(purchaserId, request);
        
        // 尝试用其他采购方关闭需求
        assertThrows(BusinessException.class, () -> {
            purchaseDemandService.closeDemand(demandId, otherPurchaserId);
        });
    }

    @Test
    @DisplayName("分页查询需求列表")
    void testListDemands() {
        // 创建多个需求
        for (int i = 0; i < 5; i++) {
            PurchaseDemandRequest request = new PurchaseDemandRequest();
            request.setTitle("需求" + i);
            request.setCategoryId(i % 2 == 0 ? 1L : 2L);
            request.setSpec("规格" + i);
            request.setUnit("公斤");
            request.setQuantity(100 + i * 10);
            request.setPriceRange("5.00-6.00");
            request.setDeliveryTime(LocalDate.now().plusDays(30));
            request.setDeliveryAddress("测试地址" + i);
            request.setQualityStandard("标准" + i);
            request.setDeadline(LocalDate.now().plusDays(15));
            
            purchaseDemandService.publishDemand(purchaserId, request);
        }
        
        // 查询所有需求
        IPage<PurchaseDemand> page = purchaseDemandService.listDemands(1, 10, null, null);
        assertTrue(page.getRecords().size() >= 5);
        
        // 按分类查询
        IPage<PurchaseDemand> categoryPage = purchaseDemandService.listDemands(1, 10, 1L, null);
        assertTrue(categoryPage.getRecords().size() >= 3);
        
        // 按状态查询
        IPage<PurchaseDemand> statusPage = purchaseDemandService.listDemands(1, 10, null, "PENDING");
        assertTrue(statusPage.getRecords().size() >= 5);
    }

    @Test
    @DisplayName("查询我的需求列表")
    void testListMyDemands() {
        // 为当前采购方创建需求
        for (int i = 0; i < 3; i++) {
            PurchaseDemandRequest request = new PurchaseDemandRequest();
            request.setTitle("我的需求" + i);
            request.setCategoryId(1L);
            request.setSpec("规格" + i);
            request.setUnit("公斤");
            request.setQuantity(100 + i * 10);
            request.setPriceRange("5.00-6.00");
            request.setDeliveryTime(LocalDate.now().plusDays(30));
            request.setDeliveryAddress("测试地址" + i);
            request.setQualityStandard("标准" + i);
            request.setDeadline(LocalDate.now().plusDays(15));
            
            purchaseDemandService.publishDemand(purchaserId, request);
        }
        
        // 为其他采购方创建需求
        for (int i = 0; i < 2; i++) {
            PurchaseDemandRequest request = new PurchaseDemandRequest();
            request.setTitle("其他需求" + i);
            request.setCategoryId(2L);
            request.setSpec("规格" + i);
            request.setUnit("公斤");
            request.setQuantity(100 + i * 10);
            request.setPriceRange("5.00-6.00");
            request.setDeliveryTime(LocalDate.now().plusDays(30));
            request.setDeliveryAddress("其他地址" + i);
            request.setQualityStandard("其他标准" + i);
            request.setDeadline(LocalDate.now().plusDays(15));
            
            purchaseDemandService.publishDemand(otherPurchaserId, request);
        }
        
        // 查询当前采购方的需求
        IPage<PurchaseDemand> myDemands = purchaseDemandService.listMyDemands(purchaserId, 1, 10);
        assertEquals(3, myDemands.getRecords().size());
        
        // 验证所有需求都属于当前采购方
        for (PurchaseDemand demand : myDemands.getRecords()) {
            assertEquals(purchaserId, demand.getPurchaserId());
        }
    }
}