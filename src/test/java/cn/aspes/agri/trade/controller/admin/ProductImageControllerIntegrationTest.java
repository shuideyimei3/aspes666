package cn.aspes.agri.trade.controller.admin;

import cn.aspes.agri.trade.dto.ProductImageRequest;
import cn.aspes.agri.trade.entity.ProductImage;
import cn.aspes.agri.trade.enums.ProductImageType;
import cn.aspes.agri.trade.service.ProductImageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductImageController.class)
@DisplayName("后台管理 - 产品图片控制器集成测试")
public class ProductImageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductImageService productImageService;

    @BeforeEach
    void setUp() {
        // 模拟按产品ID查询图片列表
        List<ProductImage> imageList = new ArrayList<>();
        when(productImageService.list(any(QueryWrapper.class))).thenReturn(imageList);

        // 模拟分页查询产品图片
        Page<ProductImage> imagePage = new Page<>(1, 10);
        imagePage.setRecords(new ArrayList<>());
        imagePage.setTotal(0);
        when(productImageService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(imagePage);

        // 模拟获取单个图片
        ProductImage mockImage = new ProductImage();
        mockImage.setId(1L);
        mockImage.setProductId(1L);
        mockImage.setImageType(ProductImageType.MAIN);
        mockImage.setSort(1);
        mockImage.setImageUrl("http://example.com/image.jpg");
        when(productImageService.getOne(any(QueryWrapper.class))).thenReturn(mockImage);
        when(productImageService.getById(anyLong())).thenReturn(mockImage);

        // 模拟保存和更新操作
        when(productImageService.save(any(ProductImage.class))).thenReturn(true);
        when(productImageService.updateById(any(ProductImage.class))).thenReturn(true);
        doNothing().when(productImageService).saveProductImages(anyLong(), any());
        doNothing().when(productImageService).removeById(anyLong());

        // 模拟列表查询
        List<ProductImage> mockImages = new ArrayList<>();
        mockImages.add(mockImage);
        when(productImageService.list(any(QueryWrapper.class))).thenReturn(mockImages);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("按产品ID查询图片列表 - 成功")
    void testListByProductSuccess() throws Exception {
        mockMvc.perform(get("/api/shared/product-images/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = {"FARMER"})
    @DisplayName("按产品ID查询图片列表 - 失败 - 无权限")
    void testListByProductFailureWithoutPermission() throws Exception {
        mockMvc.perform(get("/api/shared/product-images/product/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("按产品ID查询图片列表 - 失败 - 未认证")
    void testListByProductFailureWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/shared/product-images/product/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("上传产品图片 - 成功")
    void testUploadImageSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test image content".getBytes());

        mockMvc.perform(multipart("/api/shared/product-images/upload")
                        .file(file)
                        .param("productId", "1")
                        .param("imageType", "MAIN")
                        .param("sort", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("批量上传产品图片 - 成功")
    void testBatchUploadImagesSuccess() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "requests[0].file", "test1.jpg", "image/jpeg", "test image content 1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "requests[1].file", "test2.jpg", "image/jpeg", "test image content 2".getBytes());

        mockMvc.perform(multipart("/api/shared/product-images/batch-upload")
                        .file(file1)
                        .file(file2)
                        .param("productId", "1")
                        .param("requests[0].imageType", "MAIN")
                        .param("requests[0].sort", "1")
                        .param("requests[1].imageType", "DETAIL")
                        .param("requests[1].sort", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("添加产品图片 - 成功")
    void testAddImageSuccess() throws Exception {
        ProductImage productImage = new ProductImage();
        productImage.setProductId(1L);
        productImage.setImageType(ProductImageType.MAIN);
        productImage.setSort(1);
        productImage.setImageUrl("http://example.com/image.jpg");

        mockMvc.perform(post("/api/shared/product-images")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productImage)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("修改产品图片信息 - 成功")
    void testUpdateImageSuccess() throws Exception {
        ProductImage productImage = new ProductImage();
        productImage.setProductId(1L);
        productImage.setImageType(ProductImageType.DETAIL);
        productImage.setSort(2);
        productImage.setImageUrl("http://example.com/updated-image.jpg");

        mockMvc.perform(put("/api/shared/product-images/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productImage)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("删除产品图片 - 成功")
    void testDeleteImageSuccess() throws Exception {
        mockMvc.perform(delete("/api/shared/product-images/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("调整图片顺序 - 成功")
    void testUpdateSortSuccess() throws Exception {
        mockMvc.perform(put("/api/shared/product-images/1/sort")
                        .param("sort", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页查询产品图片 - 成功")
    void testPageImagesSuccess() throws Exception {
        mockMvc.perform(get("/api/shared/product-images/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页查询产品图片 - 带过滤条件")
    void testPageImagesWithFiltersSuccess() throws Exception {
        mockMvc.perform(get("/api/shared/product-images/page")
                        .param("current", "1")
                        .param("size", "10")
                        .param("productId", "1")
                        .param("imageType", "MAIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }
}
                        .param("imageType", "MAIN")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }
}