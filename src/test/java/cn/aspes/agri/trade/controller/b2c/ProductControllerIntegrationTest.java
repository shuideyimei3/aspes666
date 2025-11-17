package cn.aspes.agri.trade.controller.b2c;

import cn.aspes.agri.trade.dto.FarmerProductRequest;
import cn.aspes.agri.trade.service.EntityVOConverter;
import cn.aspes.agri.trade.service.FarmerInfoService;
import cn.aspes.agri.trade.service.FarmerProductService;
import cn.aspes.agri.trade.vo.FarmerProductVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FarmerProductService farmerProductService;

    @MockBean
    private FarmerInfoService farmerInfoService;

    @MockBean
    private EntityVOConverter entityVOConverter;

    @BeforeEach
    void setUp() {
        // Mock farmer info service to return a valid farmer ID
        when(farmerInfoService.getByUserId(anyLong())).thenReturn(new cn.aspes.agri.trade.entity.FarmerInfo());
    }

    @Test
    @DisplayName("发布产品 - 成功")
    @WithMockUser(roles = {"FARMER"})
    void testPublishProductSuccess() throws Exception {
        // Mock service to return a product ID
        when(farmerProductService.publishProduct(anyLong(), any(FarmerProductRequest.class))).thenReturn(1L);

        FarmerProductRequest request = new FarmerProductRequest();
        request.setCategoryId(3L);
        request.setName("测试产品");
        request.setSpec("新鲜");
        request.setUnit("斤");
        request.setPrice(5.50);
        request.setMinPurchase(10);
        request.setStock(100);
        request.setProductionMethod("有机种植");
        request.setOriginAreaId(1);
        request.setDescription("优质测试产品");

        MockMultipartFile imageFile = new MockMultipartFile(
                "images", 
                "test.jpg", 
                MediaType.IMAGE_JPEG_VALUE, 
                "test image content".getBytes());

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", 
                "", 
                MediaType.APPLICATION_JSON_VALUE, 
                objectMapper.writeValueAsString(request).getBytes());

        mockMvc.perform(multipart("/api/c2c/products")
                        .file(imageFile)
                        .file(requestPart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @DisplayName("发布产品 - 失败 - 无权限")
    @WithMockUser(roles = {"PURCHASER"})
    void testPublishProductFailureWithoutPermission() throws Exception {
        FarmerProductRequest request = new FarmerProductRequest();
        request.setCategoryId(3L);
        request.setName("测试产品");
        request.setSpec("新鲜");
        request.setUnit("斤");
        request.setPrice(5.50);
        request.setMinPurchase(10);
        request.setStock(100);

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", 
                "", 
                MediaType.APPLICATION_JSON_VALUE, 
                objectMapper.writeValueAsString(request).getBytes());

        mockMvc.perform(multipart("/api/c2c/products")
                        .file(requestPart))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("发布产品 - 失败 - 未认证")
    void testPublishProductFailureWithoutAuthentication() throws Exception {
        FarmerProductRequest request = new FarmerProductRequest();
        request.setCategoryId(3L);
        request.setName("测试产品");
        request.setSpec("新鲜");
        request.setUnit("斤");
        request.setPrice(5.50);
        request.setMinPurchase(10);
        request.setStock(100);

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", 
                "", 
                MediaType.APPLICATION_JSON_VALUE, 
                objectMapper.writeValueAsString(request).getBytes());

        mockMvc.perform(multipart("/api/c2c/products")
                        .file(requestPart))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("更新产品 - 成功")
    @WithMockUser(roles = {"FARMER"})
    void testUpdateProductSuccess() throws Exception {
        // Mock service to do nothing
        doNothing().when(farmerProductService).updateProduct(anyLong(), anyLong(), any(FarmerProductRequest.class));

        FarmerProductRequest request = new FarmerProductRequest();
        request.setCategoryId(3L);
        request.setName("更新后的产品");
        request.setSpec("新鲜");
        request.setUnit("斤");
        request.setPrice(6.50);
        request.setMinPurchase(15);
        request.setStock(150);
        request.setProductionMethod("有机种植");
        request.setOriginAreaId(1);
        request.setDescription("更新后的优质测试产品");

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", 
                "", 
                MediaType.APPLICATION_JSON_VALUE, 
                objectMapper.writeValueAsString(request).getBytes());

        mockMvc.perform(multipart("/api/c2c/products/1")
                        .file(requestPart)
                        .with(req -> {
                            req.setMethod("PUT");
                            return req;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("产品上架 - 成功")
    @WithMockUser(roles = {"FARMER"})
    void testOnSaleProductSuccess() throws Exception {
        // Mock service to do nothing
        doNothing().when(farmerProductService).onSale(anyLong(), anyLong());

        mockMvc.perform(put("/api/c2c/products/1/on-sale"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("产品下架 - 成功")
    @WithMockUser(roles = {"FARMER"})
    void testOffSaleProductSuccess() throws Exception {
        // Mock service to do nothing
        doNothing().when(farmerProductService).offSale(anyLong(), anyLong());

        mockMvc.perform(put("/api/c2c/products/1/off-sale"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("查询产品列表 - 成功")
    void testListProductsSuccess() throws Exception {
        // Mock service
        IPage<FarmerProductVO> mockPage = mock(IPage.class);
        when(mockPage.getRecords()).thenReturn(Collections.emptyList());
        when(mockPage.getTotal()).thenReturn(0L);
        
        when(farmerProductService.listProductsWithImages(anyInt(), anyInt(), any(), any(), any())).thenReturn(mockPage);

        mockMvc.perform(get("/api/c2c/products")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("查询产品列表 - 带过滤条件")
    void testListProductsWithFilters() throws Exception {
        // Mock service
        IPage<FarmerProductVO> mockPage = mock(IPage.class);
        when(mockPage.getRecords()).thenReturn(Collections.emptyList());
        when(mockPage.getTotal()).thenReturn(0L);
        
        when(farmerProductService.listProductsWithImages(anyInt(), anyInt(), eq(3L), eq(1), eq("on_sale"))).thenReturn(mockPage);

        mockMvc.perform(get("/api/c2c/products")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("categoryId", "3")
                        .param("originAreaId", "1")
                        .param("status", "on_sale"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("查询我的产品列表 - 成功")
    @WithMockUser(roles = {"FARMER"})
    void testListMyProductsSuccess() throws Exception {
        // Mock service
        IPage<FarmerProductVO> mockPage = mock(IPage.class);
        when(mockPage.getRecords()).thenReturn(Collections.emptyList());
        when(mockPage.getTotal()).thenReturn(0L);
        
        when(farmerProductService.listMyProductsWithImages(anyLong(), anyInt(), anyInt())).thenReturn(mockPage);

        mockMvc.perform(get("/api/c2c/products/my")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("查询产品详情 - 成功")
    void testGetProductSuccess() throws Exception {
        // Mock service
        FarmerProductVO mockVo = new FarmerProductVO();
        mockVo.setId(1L);
        mockVo.setName("测试产品");
        
        when(farmerProductService.getProductWithImagesById(1L)).thenReturn(mockVo);

        mockMvc.perform(get("/api/c2c/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试产品"));
    }

    @Test
    @DisplayName("查询产品详情 - 失败 - 产品不存在")
    void testGetProductFailureWithNonexistentProduct() throws Exception {
        // Mock service to return null
        when(farmerProductService.getProductWithImagesById(99999L)).thenReturn(null);

        mockMvc.perform(get("/api/c2c/products/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}