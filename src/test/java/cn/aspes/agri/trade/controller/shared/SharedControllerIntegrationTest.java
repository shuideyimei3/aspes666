package cn.aspes.agri.trade.controller.shared;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.dto.DockingFeedbackRequest;
import cn.aspes.agri.trade.dto.DockingRecordRequest;
import cn.aspes.agri.trade.entity.*;
import cn.aspes.agri.trade.enums.ProductImageType;
import cn.aspes.agri.trade.enums.UserRole;
import cn.aspes.agri.trade.security.CustomUserDetails;
import cn.aspes.agri.trade.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@Rollback
@DisplayName("Shared控制器集成测试")
public class SharedControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;
    
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FarmerInfoService farmerInfoService;
    
    @Autowired
    private PurchaserInfoService purchaserInfoService;
    
    @Autowired
    private DockingRecordService dockingRecordService;
    
    @Autowired
    private OriginAreaService originAreaService;
    
    @Autowired
    private ProductCategoryService productCategoryService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductImageService productImageService;
    
    @Autowired
    private PurchaseDemandService purchaseDemandService;
    
    @Autowired
    private StockReservationService stockReservationService;
    
    private User adminUser;
    private User farmerUser;
    private User purchaserUser;
    private FarmerInfo farmerInfo;
    private PurchaserInfo purchaserInfo;
    private OriginArea originArea;
    private ProductCategory productCategory;
    private Product product;
    private ProductImage productImage;
    private PurchaseDemand purchaseDemand;
    private DockingRecord dockingRecord;
    private StockReservation stockReservation;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        
        // 创建管理员用户
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword("password");
        adminUser.setRole("ADMIN");
        adminUser.setIsDelete(0);
        userService.save(adminUser);
        
        // 创建农户用户
        farmerUser = new User();
        farmerUser.setUsername("farmer");
        farmerUser.setPassword("password");
        farmerUser.setRole("FARMER");
        farmerUser.setIsDelete(0);
        userService.save(farmerUser);
        
        // 创建采购方用户
        purchaserUser = new User();
        purchaserUser.setUsername("purchaser");
        purchaserUser.setPassword("password");
        purchaserUser.setRole("PURCHASER");
        purchaserUser.setIsDelete(0);
        userService.save(purchaserUser);
        
        // 创建普通用户
        User normalUser = new User();
        normalUser.setUsername("user");
        normalUser.setPassword("password");
        normalUser.setRole("USER");
        normalUser.setIsDelete(0);
        userService.save(normalUser);
        
        // 创建农户信息
        farmerInfo = new FarmerInfo();
        farmerInfo.setUserId(farmerUser.getId());
        farmerInfo.setName("测试农户");
        farmerInfo.setAddress("测试地址");
        farmerInfo.setPhone("13800138000");
        farmerInfo.setAuditStatus("APPROVED");
        farmerInfo.setOriginAreaId(1);
        farmerInfoService.save(farmerInfo);
        
        // 创建采购方信息
        purchaserInfo = new PurchaserInfo();
        purchaserInfo.setUserId(purchaserUser.getId());
        purchaserInfo.setName("测试采购方");
        purchaserInfo.setAddress("测试地址");
        purchaserInfo.setPhone("13900139000");
        purchaserInfo.setAuditStatus("APPROVED");
        purchaserInfoService.save(purchaserInfo);
        
        // 创建产地
        originArea = new OriginArea();
        originArea.setProvince("四川省");
        originArea.setCity("成都市");
        originArea.setDistrict("郫都区");
        originArea.setAddress("详细地址");
        originArea.setIsPovertyArea(false);
        originAreaService.save(originArea);
        
        // 创建产品分类
        productCategory = new ProductCategory();
        productCategory.setName("蔬菜");
        productCategory.setParentId(0L);
        productCategory.setSort(1);
        productCategoryService.save(productCategory);
        
        // 创建产品
        product = new Product();
        product.setFarmerId(farmerInfo.getId());
        product.setCategoryId(productCategory.getId());
        product.setName("白菜");
        product.setDescription("新鲜白菜");
        product.setPrice(2.5);
        product.setStock(100);
        product.setUnit("斤");
        product.setOriginAreaId(originArea.getId());
        product.setStatus("ON_SALE");
        productService.save(product);
        
        // 创建产品图片
        productImage = new ProductImage();
        productImage.setProductId(product.getId());
        productImage.setImageUrl("http://example.com/image.jpg");
        productImage.setImageType(ProductImageType.MAIN);
        productImage.setSort(1);
        productImageService.save(productImage);
        
        // 创建采购需求
        purchaseDemand = new PurchaseDemand();
        purchaseDemand.setPurchaserId(purchaserInfo.getId());
        purchaseDemand.setCategoryId(productCategory.getId());
        purchaseDemand.setTitle("采购白菜");
        purchaseDemand.setDescription("需要采购新鲜白菜");
        purchaseDemand.setQuantity(50);
        purchaseDemand.setUnit("斤");
        purchaseDemand.setMaxPrice(3.0);
        purchaseDemand.setDeliveryAddress("配送地址");
        purchaseDemand.setDeliveryDate(new Date());
        purchaseDemand.setStatus("OPEN");
        purchaseDemandService.save(purchaseDemand);
        
        // 创建对接记录
        dockingRecord = new DockingRecord();
        dockingRecord.setDemandId(purchaseDemand.getId());
        dockingRecord.setFarmerId(farmerInfo.getId());
        dockingRecord.setProductId(product.getId());
        dockingRecord.setPrice(2.8);
        dockingRecord.setQuantity(30);
        dockingRecord.setUnit("斤");
        dockingRecord.setStatus("PENDING");
        dockingRecordService.save(dockingRecord);
        
        // 创建库存预留
        stockReservation = new StockReservation();
        stockReservation.setProductId(product.getId());
        stockReservation.setQuantity(30);
        stockReservation.setReservedBy(purchaserInfo.getId());
        stockReservation.setStatus("PENDING");
        stockReservationService.save(stockReservation);
    }
    
    // 对接记录控制器测试
    @Test
    @WithMockUser(username = "farmer", roles = {"FARMER"})
    @DisplayName("农户响应需求")
    void testRespondToDemand() throws Exception {
        DockingRecordRequest request = new DockingRecordRequest();
        request.setDemandId(purchaseDemand.getId());
        request.setProductId(product.getId());
        request.setPrice(2.8);
        request.setQuantity(30);
        request.setUnit("斤");
        request.setRemark("优质产品");
        
        mockMvc.perform(post("/api/shared/dockings/respond")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser(username = "purchaser", roles = {"PURCHASER"})
    @DisplayName("采购方处理对接")
    void testHandleDocking() throws Exception {
        DockingFeedbackRequest request = new DockingFeedbackRequest();
        request.setStatus("ACCEPTED");
        request.setRemark("接受报价");
        
        mockMvc.perform(put("/api/shared/dockings/{dockingId}/handle", dockingRecord.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @DisplayName("查询需求的对接列表")
    void testListByDemand() throws Exception {
        mockMvc.perform(get("/api/shared/dockings/demand/{demandId}", purchaseDemand.getId())
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray());
    }
    
    @Test
    @WithMockUser(username = "farmer", roles = {"FARMER"})
    @DisplayName("农户查询我的对接记录")
    void testListMyDockingsAsFarmer() throws Exception {
        mockMvc.perform(get("/api/shared/dockings/my")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray());
    }
    
    @Test
    @WithMockUser(username = "purchaser", roles = {"PURCHASER"})
    @DisplayName("采购方查询我的对接记录")
    void testListMyDockingsAsPurchaser() throws Exception {
        mockMvc.perform(get("/api/shared/dockings/my")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray());
    }
    
    // 产地管理控制器测试
    @Test
    @DisplayName("分页查询产地")
    void testPageOriginAreas() throws Exception {
        mockMvc.perform(get("/api/shared/origin-area/page")
                .param("current", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray());
    }
    
    @Test
    @DisplayName("按省份查询产地")
    void testPageOriginAreasByProvince() throws Exception {
        mockMvc.perform(get("/api/shared/origin-area/page")
                .param("current", "1")
                .param("size", "10")
                .param("province", "四川省"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray());
    }
    
    @Test
    @DisplayName("获取产地详情")
    void testGetOriginAreaById() throws Exception {
        mockMvc.perform(get("/api/shared/origin-area/{id}", originArea.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(originArea.getId()));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员新增产地")
    void testSaveOriginArea() throws Exception {
        OriginArea newArea = new OriginArea();
        newArea.setProvince("云南省");
        newArea.setCity("昆明市");
        newArea.setDistrict("五华区");
        newArea.setAddress("详细地址");
        newArea.setIsPovertyArea(true);
        
        mockMvc.perform(post("/api/shared/origin-area")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newArea)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员更新产地")
    void testUpdateOriginArea() throws Exception {
        originArea.setProvince("云南省");
        
        mockMvc.perform(put("/api/shared/origin-area")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(originArea)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员删除产地")
    void testDeleteOriginArea() throws Exception {
        mockMvc.perform(delete("/api/shared/origin-area/{id}", originArea.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    // 产品分类控制器测试
    @Test
    @DisplayName("获取树形分类列表")
    void testGetProductCategoryTree() throws Exception {
        mockMvc.perform(get("/api/shared/product-category/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    @DisplayName("获取分类详情")
    void testGetProductCategoryById() throws Exception {
        mockMvc.perform(get("/api/shared/product-category/{id}", productCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(productCategory.getId()));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员新增分类")
    void testSaveProductCategory() throws Exception {
        ProductCategory newCategory = new ProductCategory();
        newCategory.setName("水果");
        newCategory.setParentId(0L);
        newCategory.setSort(2);
        
        mockMvc.perform(post("/api/shared/product-category")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员更新分类")
    void testUpdateProductCategory() throws Exception {
        productCategory.setName("蔬菜类");
        
        mockMvc.perform(put("/api/shared/product-category")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("管理员删除分类")
    void testDeleteProductCategory() throws Exception {
        mockMvc.perform(delete("/api/shared/product-category/{id}", productCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    // 产品图片控制器测试
    @Test
    @DisplayName("按产品ID查询图片列表")
    void testListProductImagesByProductId() throws Exception {
        mockMvc.perform(get("/api/shared/product-images/product/{productId}", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    @DisplayName("添加产品图片")
    void testAddProductImage() throws Exception {
        ProductImage newImage = new ProductImage();
        newImage.setProductId(product.getId());
        newImage.setImageUrl("http://example.com/new-image.jpg");
        newImage.setImageType(ProductImageType.DETAIL);
        newImage.setSort(2);
        
        mockMvc.perform(post("/api/shared/product-images")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newImage)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @DisplayName("修改产品图片信息")
    void testUpdateProductImage() throws Exception {
        productImage.setImageUrl("http://example.com/updated-image.jpg");
        
        mockMvc.perform(put("/api/shared/product-images/{id}", productImage.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productImage)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @DisplayName("删除产品图片")
    void testDeleteProductImage() throws Exception {
        mockMvc.perform(delete("/api/shared/product-images/{id}", productImage.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @DisplayName("调整图片顺序")
    void testUpdateProductImageSort() throws Exception {
        mockMvc.perform(put("/api/shared/product-images/{id}/sort", productImage.getId())
                .param("sort", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @DisplayName("分页查询产品图片")
    void testPageProductImages() throws Exception {
        mockMvc.perform(get("/api/shared/product-images/page")
                .param("current", "1")
                .param("size", "10")
                .param("productId", product.getId().toString())
                .param("imageType", "MAIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray());
    }
    
    // 库存预留控制器测试
    @Test
    @DisplayName("获取订单的库存预留记录")
    void testGetReservationByOrder() throws Exception {
        // 假设有一个订单ID
        Long orderId = 1L;
        
        mockMvc.perform(get("/api/shared/stock-reservation/order/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser(roles = {"FARMER"})
    @DisplayName("农户确认库存预留")
    void testConfirmReservationAsFarmer() throws Exception {
        mockMvc.perform(put("/api/shared/stock-reservation/{reservationId}/confirm", stockReservation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser(roles = {"PURCHASER"})
    @DisplayName("采购方确认库存预留")
    void testConfirmReservationAsPurchaser() throws Exception {
        mockMvc.perform(put("/api/shared/stock-reservation/{reservationId}/confirm", stockReservation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser(roles = {"USER"})
    @DisplayName("普通用户确认库存预留应被拒绝")
    void testConfirmReservationAccessDenied() throws Exception {
        mockMvc.perform(put("/api/shared/stock-reservation/{reservationId}/confirm", stockReservation.getId()))
                .andExpect(status().isForbidden());
    }
}