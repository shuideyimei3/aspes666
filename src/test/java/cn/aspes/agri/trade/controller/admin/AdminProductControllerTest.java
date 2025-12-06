package cn.aspes.agri.trade.controller.admin;

import cn.aspes.agri.trade.BaseTest;
import cn.aspes.agri.trade.entity.FarmerProduct;
import cn.aspes.agri.trade.enums.ProductStatus;
import cn.aspes.agri.trade.service.FarmerProductService;
import cn.aspes.agri.trade.vo.FarmerProductVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("管理员产品控制器测试")
class AdminProductControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FarmerProductService farmerProductService;

    @Autowired
    private ObjectMapper objectMapper;

    private FarmerProduct testProduct;
    private FarmerProductVO testProductVO;
    private IPage<FarmerProductVO> testPage;

    @BeforeEach
    void setUp() {
        // 创建测试产品
        testProduct = new FarmerProduct();
        testProduct.setId(1L);
        testProduct.setFarmerId(1L);
        testProduct.setCategoryId(2L);
        testProduct.setOriginAreaId(3);
        testProduct.setName("测试产品");
        testProduct.setDescription("测试产品描述");
        testProduct.setPrice(new java.math.BigDecimal("10.00"));
        testProduct.setStock(100);
        testProduct.setStatus(ProductStatus.ON_SALE);
        
        // 创建测试产品VO
        testProductVO = new FarmerProductVO();
        testProductVO.setId(1L);
        testProductVO.setFarmerId(1L);
        testProductVO.setCategoryId(2L);
        testProductVO.setOriginAreaId(3);
        testProductVO.setName("测试产品");
        testProductVO.setDescription("测试产品描述");
        testProductVO.setPrice(new java.math.BigDecimal("10.00"));
        testProductVO.setStock(100);
        testProductVO.setStatus(ProductStatus.ON_SALE);
        
        // 创建测试分页结果
        testPage = new Page<>(1, 10);
        testPage.setRecords(Arrays.asList(testProductVO));
        testPage.setTotal(1);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页查询所有产品 - 成功")
    void pageProducts_Success() throws Exception {
        // 模拟服务返回
        when(farmerProductService.listProductsWithImages(anyInt(), anyInt(), anyLong(), anyInt(), anyString()))
                .thenReturn(testPage);

        // 执行请求并验证结果
        mockMvc.perform(get("/api/admin/products/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("categoryId", "2")
                        .param("originAreaId", "3")
                        .param("status", "ON_SALE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.records[0].name").value("测试产品"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("查询产品详情 - 成功")
    void getProductDetail_Success() throws Exception {
        // 模拟服务返回
        when(farmerProductService.getProductWithImagesById(1L)).thenReturn(testProductVO);

        // 执行请求并验证结果
        mockMvc.perform(get("/api/admin/products/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试产品"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("强制产品下架 - 成功")
    void forceOffSale_Success() throws Exception {
        // 模拟服务返回
        when(farmerProductService.getProductById(1L)).thenReturn(testProduct);

        // 执行请求并验证结果
        mockMvc.perform(put("/api/admin/products/1/force-off-sale")
                        .param("reason", "测试下架原因")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("强制产品上架 - 成功")
    void forceOnSale_Success() throws Exception {
        // 模拟服务返回
        when(farmerProductService.getProductById(1L)).thenReturn(testProduct);

        // 执行请求并验证结果
        mockMvc.perform(put("/api/admin/products/1/force-on-sale")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("删除产品 - 成功")
    void deleteProduct_Success() throws Exception {
        // 模拟服务返回
        when(farmerProductService.getProductById(1L)).thenReturn(testProduct);

        // 执行请求并验证结果
        mockMvc.perform(delete("/api/admin/products/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据产品名称搜索产品 - 成功")
    void searchProducts_Success() throws Exception {
        // 创建测试分页结果
        IPage<FarmerProduct> searchResult = new Page<>(1, 10);
        searchResult.setRecords(Arrays.asList(testProduct));
        searchResult.setTotal(1);
        
        // 模拟服务返回
        when(farmerProductService.searchProductsByName(anyString(), anyInt(), anyInt()))
                .thenReturn(searchResult);

        // 执行请求并验证结果
        mockMvc.perform(get("/api/admin/products/search")
                        .param("keyword", "测试")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.records[0].name").value("测试产品"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("获取产品统计信息 - 成功")
    void getProductStatistics_Success() throws Exception {
        // 模拟服务返回
        when(farmerProductService.count()).thenReturn(10L);
        when(farmerProductService.count(any())).thenReturn(7L);

        // 执行请求并验证结果
        mockMvc.perform(get("/api/admin/products/statistics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCount").value(10))
                .andExpect(jsonPath("$.data.onlineCount").value(7))
                .andExpect(jsonPath("$.data.offlineCount").value(7));
    }

    @Test
    @DisplayName("未授权访问 - 失败")
    void unauthorizedAccess_Failure() throws Exception {
        // 不使用@WithMockUser注解，模拟未授权访问
        mockMvc.perform(get("/api/admin/products/page")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}