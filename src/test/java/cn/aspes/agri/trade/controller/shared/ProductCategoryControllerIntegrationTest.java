package cn.aspes.agri.trade.controller.shared;

import cn.aspes.agri.trade.dto.ProductCategoryRequest;
import cn.aspes.agri.trade.entity.ProductCategory;
import cn.aspes.agri.trade.service.ProductCategoryService;
import cn.aspes.agri.trade.vo.ProductCategoryVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductCategoryController.class)
@DisplayName("共享 - 产品分类管理控制器集成测试")
class ProductCategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductCategoryService productCategoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductCategory mockProductCategory;
    private ProductCategoryVO mockProductCategoryVO;
    private List<ProductCategoryVO> mockCategoryTree;

    @BeforeEach
    void setUp() {
        // 创建模拟的产品分类
        mockProductCategory = new ProductCategory();
        mockProductCategory.setId(1L);
        mockProductCategory.setName("蔬菜类");
        mockProductCategory.setParentId(0L);
        mockProductCategory.setIcon("vegetables");
        mockProductCategory.setSort(1);
        mockProductCategory.setDescription("新鲜蔬菜分类");

        // 创建模拟的产品分类VO
        mockProductCategoryVO = new ProductCategoryVO();
        mockProductCategoryVO.setId(1L);
        mockProductCategoryVO.setName("蔬菜类");
        mockProductCategoryVO.setParentId(0L);
        mockProductCategoryVO.setIcon("vegetables");
        mockProductCategoryVO.setSort(1);
        mockProductCategoryVO.setDescription("新鲜蔬菜分类");

        // 创建模拟的分类树
        mockCategoryTree = new ArrayList<>();
        mockCategoryTree.add(mockProductCategoryVO);
    }

    @Test
    @DisplayName("获取树形分类列表 - 成功")
    void getCategoryTree_Success() throws Exception {
        when(productCategoryService.getCategoryTree()).thenReturn(mockCategoryTree);

        mockMvc.perform(get("/api/shared/categories/tree"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("获取分类详情 - 成功")
    void getCategoryDetail_Success() throws Exception {
        when(productCategoryService.getCategoryById(anyLong())).thenReturn(mockProductCategoryVO);

        mockMvc.perform(get("/api/shared/categories/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("获取分类详情 - 失败 - 分类不存在")
    void getCategoryDetail_NotFound() throws Exception {
        when(productCategoryService.getCategoryById(anyLong())).thenReturn(null);

        mockMvc.perform(get("/api/shared/categories/99999"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("新增分类 - 成功")
    void addCategory_Admin_Success() throws Exception {
        ProductCategoryRequest request = new ProductCategoryRequest();
        request.setName("测试分类");
        request.setParentId(0L);
        request.setIcon("test-icon");
        request.setSort(1);
        request.setDescription("测试分类描述");

        when(productCategoryService.addCategory(any(ProductCategoryRequest.class))).thenReturn(1L);

        mockMvc.perform(post("/api/shared/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @WithMockUser(roles = {"FARMER"})
    @DisplayName("新增分类 - 无权限")
    void addCategory_Farmer_Forbidden() throws Exception {
        ProductCategoryRequest request = new ProductCategoryRequest();
        request.setName("测试分类");
        request.setParentId(0L);

        mockMvc.perform(post("/api/shared/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("更新分类 - 成功")
    void updateCategory_Admin_Success() throws Exception {
        ProductCategoryRequest request = new ProductCategoryRequest();
        request.setName("更新后的分类");
        request.setIcon("updated-icon");
        request.setDescription("更新后的描述");

        when(productCategoryService.updateCategory(anyLong(), any(ProductCategoryRequest.class))).thenReturn(true);

        mockMvc.perform(put("/api/shared/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"PURCHASER"})
    @DisplayName("更新分类 - 无权限")
    void updateCategory_Purchaser_Forbidden() throws Exception {
        ProductCategoryRequest request = new ProductCategoryRequest();
        request.setName("更新后的分类");

        mockMvc.perform(put("/api/shared/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("删除分类 - 成功")
    void deleteCategory_Admin_Success() throws Exception {
        doNothing().when(productCategoryService).deleteCategory(anyLong());

        mockMvc.perform(delete("/api/shared/categories/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"FARMER"})
    @DisplayName("删除分类 - 无权限")
    void deleteCategory_Farmer_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/shared/categories/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("删除分类 - 未认证")
    void deleteCategory_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/shared/categories/1"))
                .andExpect(status().isUnauthorized());
    }
}