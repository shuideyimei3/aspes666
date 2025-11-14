package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.config.TestDatabaseConfig;
import cn.aspes.agri.trade.dto.UserRegisterRequest;
import cn.aspes.agri.trade.entity.PurchaseContract;
import cn.aspes.agri.trade.enums.ContractStatus;
import cn.aspes.agri.trade.enums.UserRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
 * 采购合同服务集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestDatabaseConfig.class)
@Transactional
class ContractIntegrationTest {

    @Autowired
    private PurchaseContractService purchaseContractService;

    @Autowired
    private UserService userService;

    private Long farmerId;
    private Long purchaserId;

    @BeforeEach
    void setUp() {
        // 创建农民用户
        UserRegisterRequest farmerRequest = new UserRegisterRequest();
        farmerRequest.setUsername("farmer_contract" + System.currentTimeMillis());
        farmerRequest.setPassword("password123");
        farmerRequest.setRole(UserRole.FARMER);
        farmerRequest.setContactPerson("张三");
        farmerRequest.setContactPhone("13800138000");
        farmerRequest.setContactEmail("farmer@example.com");
        farmerId = userService.register(farmerRequest);

        // 创建采购方用户
        UserRegisterRequest purchaserRequest = new UserRegisterRequest();
        purchaserRequest.setUsername("purchaser_contract" + System.currentTimeMillis());
        purchaserRequest.setPassword("password123");
        purchaserRequest.setRole(UserRole.PURCHASER);
        purchaserRequest.setContactPerson("李四");
        purchaserRequest.setContactPhone("13800138001");
        purchaserRequest.setContactEmail("purchaser@example.com");
        purchaserId = userService.register(purchaserRequest);
    }

    @Test
    void testCreateContract() {
        // 直接创建合同实体
        PurchaseContract contract = new PurchaseContract();
        contract.setContractNo("CT-" + System.currentTimeMillis());
        contract.setPurchaserId(purchaserId);
        contract.setFarmerId(farmerId);
        contract.setTotalAmount(new BigDecimal("500.00"));
        contract.setPaymentTerms("30天内支付");
        contract.setDeliveryTime(LocalDate.now().plusDays(7));
        contract.setDeliveryAddress("浙江省杭州市");
        contract.setQualityStandards("优质等级");
        contract.setBreachTerms("逾期赔偿10%");
        contract.setStatus(ContractStatus.DRAFT);

        purchaseContractService.save(contract);

        assertNotNull(contract.getId());
        PurchaseContract saved = purchaseContractService.getById(contract.getId());
        assertEquals(purchaserId, saved.getPurchaserId());
        assertEquals(farmerId, saved.getFarmerId());
        assertEquals(new BigDecimal("500.00"), saved.getTotalAmount());
    }

    @Test
    void testUpdateContractStatus() {
        // 创建合同
        PurchaseContract contract = new PurchaseContract();
        contract.setContractNo("CT-" + System.currentTimeMillis());
        contract.setPurchaserId(purchaserId);
        contract.setFarmerId(farmerId);
        contract.setTotalAmount(new BigDecimal("500.00"));
        contract.setPaymentTerms("30天内支付");
        contract.setDeliveryTime(LocalDate.now().plusDays(7));
        contract.setDeliveryAddress("浙江省杭州市");
        contract.setStatus(ContractStatus.DRAFT);

        purchaseContractService.save(contract);
        Long contractId = contract.getId();

        // 更新合同状态
        PurchaseContract updated = purchaseContractService.getById(contractId);
        updated.setStatus(ContractStatus.SIGNED);
        purchaseContractService.updateById(updated);

        PurchaseContract result = purchaseContractService.getById(contractId);
        assertEquals(ContractStatus.SIGNED, result.getStatus());
    }

    @Test
    void testListContracts() {
        // 创建多个合同
        for (int i = 0; i < 3; i++) {
            PurchaseContract contract = new PurchaseContract();
            contract.setContractNo("CT-" + System.currentTimeMillis() + "-" + amount.toString());
            contract.setPurchaserId(purchaserId);
            contract.setFarmerId(farmerId);
            contract.setTotalAmount(new BigDecimal(500 + i * 100));
            contract.setPaymentTerms("30天内支付");
            contract.setDeliveryTime(LocalDate.now().plusDays(7 + i));
            contract.setDeliveryAddress("浙江省杭州市");
            contract.setStatus(ContractStatus.DRAFT);

            purchaseContractService.save(contract);
        }

        // 分页查询合同
        Page<PurchaseContract> page = purchaseContractService.pageContracts(1, 10, null);
        assertTrue(page.getRecords().size() >= 3);
    }

    @Test
    void testListContractsByStatus() {
        // 创建多个合同，其中一些签署
        for (int i = 0; i < 2; i++) {
            PurchaseContract contract = new PurchaseContract();
            contract.setContractNo("CT-" + System.currentTimeMillis() + "-" + amount.toString());
            contract.setPurchaserId(purchaserId);
            contract.setFarmerId(farmerId);
            contract.setTotalAmount(new BigDecimal(500 + i * 100));
            contract.setPaymentTerms("30天内支付");
            contract.setDeliveryTime(LocalDate.now().plusDays(7 + i));
            contract.setDeliveryAddress("浙江省杭州市");
            contract.setStatus(ContractStatus.DRAFT);

            purchaseContractService.save(contract);
        }

        // 创建已签署的合同
        PurchaseContract signedContract = new PurchaseContract();
        signedContract.setContractNo("CT-" + System.currentTimeMillis());
        signedContract.setPurchaserId(purchaserId);
        signedContract.setFarmerId(farmerId);
        signedContract.setTotalAmount(new BigDecimal("800.00"));
        signedContract.setPaymentTerms("30天内支付");
        signedContract.setDeliveryTime(LocalDate.now().plusDays(9));
        signedContract.setDeliveryAddress("浙江省杭州市");
        signedContract.setStatus(ContractStatus.SIGNED);
        purchaseContractService.save(signedContract);

        // 按状态查询
        Page<PurchaseContract> signedPage = purchaseContractService.pageContracts(1, 10, ContractStatus.SIGNED.getCode());
        assertTrue(signedPage.getRecords().size() > 0);
        for (PurchaseContract contract : signedPage.getRecords()) {
            assertEquals(ContractStatus.SIGNED, contract.getStatus());
        }
    }

    @Test
    void testGetContractDetail() {
        // 创建合同
        PurchaseContract contract = new PurchaseContract();
        contract.setContractNo("CT-" + System.currentTimeMillis());
        contract.setPurchaserId(purchaserId);
        contract.setFarmerId(farmerId);
        contract.setTotalAmount(new BigDecimal("500.00"));
        contract.setPaymentTerms("30天内支付");
        contract.setDeliveryTime(LocalDate.now().plusDays(7));
        contract.setDeliveryAddress("浙江省杭州市");
        contract.setQualityStandards("优质等级");
        contract.setBreachTerms("逾期赔偿10%");
        contract.setStatus(ContractStatus.SIGNED);

        purchaseContractService.save(contract);

        // 获取合同详情
        PurchaseContract detail = purchaseContractService.getContractDetail(contract.getId());
        assertNotNull(detail);
        assertEquals(contract.getId(), detail.getId());
        assertEquals(farmerId, detail.getFarmerId());
        assertEquals(purchaserId, detail.getPurchaserId());
        assertEquals(new BigDecimal("500.00"), detail.getTotalAmount());
    }

    @Test
    void testContractStatusProgression() {
        // 创建合同
        PurchaseContract contract = new PurchaseContract();
        contract.setContractNo("CT-" + System.currentTimeMillis());
        contract.setPurchaserId(purchaserId);
        contract.setFarmerId(farmerId);
        contract.setTotalAmount(new BigDecimal("1000.00"));
        contract.setPaymentTerms("60天内支付");
        contract.setDeliveryTime(LocalDate.now().plusDays(14));
        contract.setDeliveryAddress("浙江省杭州市");
        contract.setQualityStandards("特级等级");
        contract.setBreachTerms("逾期赔偿15%");
        contract.setStatus(ContractStatus.DRAFT);

        purchaseContractService.save(contract);
        Long contractId = contract.getId();

        // 验证初始状态
        PurchaseContract current = purchaseContractService.getById(contractId);
        assertEquals(ContractStatus.DRAFT, current.getStatus());

        // 更新为已签署
        current.setStatus(ContractStatus.SIGNED);
        purchaseContractService.updateById(current);

        current = purchaseContractService.getById(contractId);
        assertEquals(ContractStatus.SIGNED, current.getStatus());

        // 更新为执行中
        current.setStatus(ContractStatus.EXECUTING);
        purchaseContractService.updateById(current);

        current = purchaseContractService.getById(contractId);
        assertEquals(ContractStatus.EXECUTING, current.getStatus());

        // 更新为已完成
        current.setStatus(ContractStatus.COMPLETED);
        purchaseContractService.updateById(current);

        current = purchaseContractService.getById(contractId);
        assertEquals(ContractStatus.COMPLETED, current.getStatus());
    }

    @Test
    void testMultipleContractsForSameFarmer() {
        // 为同一农民创建多个合同
        for (int i = 0; i < 5; i++) {
            PurchaseContract contract = new PurchaseContract();
            contract.setContractNo("CT-" + System.currentTimeMillis() + "-" + amount.toString());
            contract.setPurchaserId(purchaserId + i); // 不同采购方
            contract.setFarmerId(farmerId); // 相同农民
            contract.setTotalAmount(new BigDecimal(500 + i * 100));
            contract.setPaymentTerms("30天内支付");
            contract.setDeliveryTime(LocalDate.now().plusDays(7 + i));
            contract.setDeliveryAddress("浙江省杭州市");
            contract.setStatus(ContractStatus.DRAFT);

            purchaseContractService.save(contract);
        }

        // 查询农民的所有合同
        Page<PurchaseContract> page = purchaseContractService.pageContracts(1, 20, null);
        assertTrue(page.getRecords().size() >= 5);
    }

    @Test
    void testContractAmountVariations() {
        // 测试不同金额的合同
        BigDecimal[] amounts = {
                new BigDecimal("100.00"),
                new BigDecimal("500.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("10000.00")
        };

        for (BigDecimal amount : amounts) {
            PurchaseContract contract = new PurchaseContract();
            contract.setContractNo("CT-" + System.currentTimeMillis() + "-" + amount.toString());
            contract.setPurchaserId(purchaserId);
            contract.setFarmerId(farmerId);
            contract.setTotalAmount(amount);
            contract.setPaymentTerms("30天内支付");
            contract.setDeliveryTime(LocalDate.now().plusDays(7));
            contract.setDeliveryAddress("浙江省杭州市");
            contract.setStatus(ContractStatus.DRAFT);

            purchaseContractService.save(contract);
        }

        // 验证所有合同都被保存
        Page<PurchaseContract> page = purchaseContractService.pageContracts(1, 50, null);
        assertTrue(page.getRecords().size() >= 5);

        // 检查是否包含所有金额
        for (BigDecimal expectedAmount : amounts) {
            boolean found = page.getRecords().stream()
                    .anyMatch(c -> expectedAmount.compareTo(c.getTotalAmount()) == 0);
            assertTrue(found);
        }
    }
}
