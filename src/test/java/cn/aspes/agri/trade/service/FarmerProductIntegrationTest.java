package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.dto.UserRegisterRequest;
import cn.aspes.agri.trade.entity.FarmerInfo;
import cn.aspes.agri.trade.entity.FarmerProduct;
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
 * 农产品服务集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FarmerProductIntegrationTest {

    @Autowired
    private FarmerProductService farmerProductService;

    @Autowired
    private FarmerInfoService farmerInfoService;

    @Autowired
    private UserService userService;

    private Long farmerId;

    @BeforeEach
    void setUp() {
        // 创建农民用户
        UserRegisterRequest farmerRequest = new UserRegisterRequest();
        farmerRequest.setUsername("farmer_prod" + System.currentTimeMillis());
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
    }

    @Test
    void testCreateProduct() {
        FarmerProduct product = new FarmerProduct();
        product.setFarmerId(farmerId);
        product.setCategoryId(1L);
        product.setOriginAreaId(1);
        product.setName("水稻");
        product.setSpec("优质水稻");
        product.setUnit("公斤");
        product.setPrice(new BigDecimal("5.50"));
        product.setMinPurchase(10);
        product.setStock(1000);
        product.setProductionDate(LocalDate.now().minusDays(30));
        product.setShelfLife("12个月");
        product.setProductionMethod("有机种植");
        product.setDescription("优质有机水稻，来自示范农场");
        product.setStatus(ProductStatus.ON_SALE);

        farmerProductService.save(product);

        assertNotNull(product.getId());
        FarmerProduct savedProduct = farmerProductService.getById(product.getId());
        assertEquals("水稻", savedProduct.getName());
        assertEquals(ProductStatus.ON_SALE, savedProduct.getStatus());
    }

    @Test
    void testUpdateProduct() {
        // 创建产品
        FarmerProduct product = new FarmerProduct();
        product.setFarmerId(farmerId);
        product.setCategoryId(1L);
        product.setOriginAreaId(1);
        product.setName("玉米");
        product.setSpec("黄玉米");
        product.setUnit("公斤");
        product.setPrice(new BigDecimal("4.00"));
        product.setMinPurchase(20);
        product.setStock(500);
        product.setStatus(ProductStatus.ON_SALE);

        farmerProductService.save(product);
        Long productId = product.getId();

        // 更新产品信息
        FarmerProduct updateProduct = farmerProductService.getById(productId);
        updateProduct.setPrice(new BigDecimal("4.50"));
        updateProduct.setStock(800);
        updateProduct.setDescription("优质黄玉米，产自示范农场");

        farmerProductService.updateById(updateProduct);

        FarmerProduct result = farmerProductService.getById(productId);
        assertEquals(new BigDecimal("4.50"), result.getPrice());
        assertEquals(800, result.getStock());
        assertEquals("优质黄玉米，产自示范农场", result.getDescription());
    }

    @Test
    void testListFarmerProducts() {
        // 创建多个产品
        for (int i = 0; i < 3; i++) {
            FarmerProduct product = new FarmerProduct();
            product.setFarmerId(farmerId);
            product.setCategoryId(1L);
            product.setOriginAreaId(1);
            product.setName("产品" + i);
            product.setSpec("规格" + i);
            product.setUnit("公斤");
            product.setPrice(new BigDecimal(5 + i));
            product.setMinPurchase(10);
            product.setStock(1000 - i * 100);
            product.setStatus(ProductStatus.ON_SALE);

            farmerProductService.save(product);
        }

        // 查询农民的产品列表
        Page<FarmerProduct> page = farmerProductService.page(new Page<>(1, 10));
        assertTrue(page.getRecords().size() >= 3);
    }

    @Test
    void testChangeProductStatus() {
        // 创建产品
        FarmerProduct product = new FarmerProduct();
        product.setFarmerId(farmerId);
        product.setCategoryId(1L);
        product.setOriginAreaId(1);
        product.setName("小麦");
        product.setSpec("高筋小麦");
        product.setUnit("公斤");
        product.setPrice(new BigDecimal("6.00"));
        product.setMinPurchase(15);
        product.setStock(2000);
        product.setStatus(ProductStatus.ON_SALE);

        farmerProductService.save(product);
        Long productId = product.getId();

        // 下架产品
        FarmerProduct updateProduct = farmerProductService.getById(productId);
        updateProduct.setStatus(ProductStatus.OFF_SALE);
        farmerProductService.updateById(updateProduct);

        FarmerProduct result = farmerProductService.getById(productId);
        assertEquals(ProductStatus.OFF_SALE, result.getStatus());

        // 重新上架
        updateProduct = farmerProductService.getById(productId);
        updateProduct.setStatus(ProductStatus.ON_SALE);
        farmerProductService.updateById(updateProduct);

        result = farmerProductService.getById(productId);
        assertEquals(ProductStatus.ON_SALE, result.getStatus());
    }

    @Test
    void testProductStockManagement() {
        // 创建产品
        FarmerProduct product = new FarmerProduct();
        product.setFarmerId(farmerId);
        product.setCategoryId(1L);
        product.setOriginAreaId(1);
        product.setName("蔬菜");
        product.setSpec("有机蔬菜");
        product.setUnit("公斤");
        product.setPrice(new BigDecimal("8.00"));
        product.setMinPurchase(5);
        product.setStock(500);
        product.setStatus(ProductStatus.ON_SALE);

        farmerProductService.save(product);
        Long productId = product.getId();

        // 验证初始库存
        FarmerProduct savedProduct = farmerProductService.getById(productId);
        assertEquals(500, savedProduct.getStock());

        // 减少库存
        savedProduct.setStock(400);
        farmerProductService.updateById(savedProduct);

        FarmerProduct updated = farmerProductService.getById(productId);
        assertEquals(400, updated.getStock());

        // 增加库存
        updated.setStock(600);
        farmerProductService.updateById(updated);

        FarmerProduct finalProduct = farmerProductService.getById(productId);
        assertEquals(600, finalProduct.getStock());
    }

    @Test
    void testProductSearch() {
        // 创建不同名称的产品
        String[] productNames = {"水稻", "玉米", "大豆", "小麦"};
        for (String name : productNames) {
            FarmerProduct product = new FarmerProduct();
            product.setFarmerId(farmerId);
            product.setCategoryId(1L);
            product.setOriginAreaId(1);
            product.setName(name);
            product.setSpec("标准规格");
            product.setUnit("公斤");
            product.setPrice(new BigDecimal("5.00"));
            product.setMinPurchase(10);
            product.setStock(1000);
            product.setStatus(ProductStatus.ON_SALE);

            farmerProductService.save(product);
        }

        // 查询农民的所有产品
        Page<FarmerProduct> page = farmerProductService.page(new Page<>(1, 20));
        assertTrue(page.getRecords().size() >= 4);
    }

    @Test
    void testProductPricing() {
        // 创建产品并验证价格信息
        FarmerProduct product = new FarmerProduct();
        product.setFarmerId(farmerId);
        product.setCategoryId(1L);
        product.setOriginAreaId(1);
        product.setName("高端产品");
        product.setSpec("优质规格");
        product.setUnit("公斤");
        product.setPrice(new BigDecimal("10.50"));
        product.setMinPurchase(20);
        product.setStock(100);
        product.setDescription("高端有机农产品");
        product.setStatus(ProductStatus.ON_SALE);

        farmerProductService.save(product);

        FarmerProduct savedProduct = farmerProductService.getById(product.getId());
        assertEquals(0, new BigDecimal("10.50").compareTo(savedProduct.getPrice()));
        assertEquals(20, savedProduct.getMinPurchase());
    }

    @Test
    void testProductDescription() {
        // 创建产品并验证描述信息
        FarmerProduct product = new FarmerProduct();
        product.setFarmerId(farmerId);
        product.setCategoryId(1L);
        product.setOriginAreaId(1);
        product.setName("特产");
        product.setSpec("地方特产");
        product.setUnit("盒");
        product.setPrice(new BigDecimal("50.00"));
        product.setMinPurchase(1);
        product.setStock(50);
        product.setProductionDate(LocalDate.now().minusMonths(1));
        product.setShelfLife("24个月");
        product.setProductionMethod("传统工艺");
        product.setDescription("地方特产，品质上乘");
        product.setStatus(ProductStatus.ON_SALE);

        farmerProductService.save(product);

        FarmerProduct savedProduct = farmerProductService.getById(product.getId());
        assertEquals("地方特产，品质上乘", savedProduct.getDescription());
        assertEquals("传统工艺", savedProduct.getProductionMethod());
        assertEquals("24个月", savedProduct.getShelfLife());
    }
}
