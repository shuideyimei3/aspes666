package cn.aspes.agri.trade.controller.shared;

import cn.aspes.agri.trade.entity.OriginArea;
import cn.aspes.agri.trade.service.OriginAreaService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OriginAreaController.class)
@DisplayName("共享 - 产地控制器集成测试")
public class OriginAreaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OriginAreaService originAreaService;

    private OriginArea mockOriginArea;

    @BeforeEach
    void setUp() {
        // 创建模拟产地对象
        mockOriginArea = new OriginArea();
        mockOriginArea.setAreaId(1L);
        mockOriginArea.setAreaCode("510125");
        mockOriginArea.setAreaName("测试产地");
        mockOriginArea.setProvince("四川省");
        mockOriginArea.setCity("成都市");
        mockOriginArea.setFeature("测试特色");
        mockOriginArea.setIsPovertyArea(false);

        // 模拟服务层方法
        Page<OriginArea> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.singletonList(mockOriginArea));
        mockPage.setTotal(1);

        when(originAreaService.pageOriginAreas(anyInt(), anyInt(), anyString(), anyString(), any())).thenReturn(mockPage);
        when(originAreaService.getById(anyLong())).thenReturn(mockOriginArea);
        when(originAreaService.save(any(OriginArea.class))).thenReturn(true);
        when(originAreaService.updateById(any(OriginArea.class))).thenReturn(true);
        doNothing().when(originAreaService).removeById(anyLong());
    }

    @Test
    @DisplayName("分页查询产地 - 成功")
    void testPageOriginAreasSuccess() throws Exception {
        mockMvc.perform(get("/api/shared/origin-area/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("分页查询产地 - 带过滤条件")
    void testPageOriginAreasWithFilters() throws Exception {
        mockMvc.perform(get("/api/shared/origin-area/page")
                        .param("current", "1")
                        .param("size", "10")
                        .param("province", "四川省")
                        .param("city", "成都市")
                        .param("isPovertyArea", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("获取产地详情 - 成功")
    void testGetOriginAreaByIdSuccess() throws Exception {
        mockMvc.perform(get("/api/shared/origin-area/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.areaId").value(1))
                .andExpect(jsonPath("$.data.areaName").value("测试产地"));
    }

    @Test
    @DisplayName("获取产地详情 - 失败 - 产地不存在")
    void testGetOriginAreaByIdFailureWithNonexistentArea() throws Exception {
        when(originAreaService.getById(anyLong())).thenReturn(null);
        
        mockMvc.perform(get("/api/shared/origin-area/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("新增产地 - 成功")
    @WithMockUser(roles = {"ADMIN"})
    void testSaveOriginAreaSuccess() throws Exception {
        OriginArea originArea = new OriginArea();
        originArea.setAreaCode("510125");
        originArea.setAreaName("测试产地");
        originArea.setProvince("四川省");
        originArea.setCity("成都市");
        originArea.setFeature("测试特色");
        originArea.setIsPovertyArea(false);

        mockMvc.perform(post("/api/shared/origin-area")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(originArea)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("新增产地 - 失败 - 无权限（农户）")
    @WithMockUser(roles = {"FARMER"})
    void testSaveOriginAreaFailureAsFarmer() throws Exception {
        OriginArea originArea = new OriginArea();
        originArea.setAreaCode("510126");
        originArea.setAreaName("测试产地2");
        originArea.setProvince("四川省");
        originArea.setCity("成都市");

        mockMvc.perform(post("/api/shared/origin-area")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(originArea)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("更新产地 - 成功")
    @WithMockUser(roles = {"ADMIN"})
    void testUpdateOriginAreaSuccess() throws Exception {
        OriginArea originArea = new OriginArea();
        originArea.setAreaId(1);
        originArea.setAreaCode("510124");
        originArea.setAreaName("更新后的产地");
        originArea.setProvince("四川省");
        originArea.setCity("成都市");
        originArea.setFeature("更新后的特色");
        originArea.setIsPovertyArea(false);

        mockMvc.perform(put("/api/shared/origin-area")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(originArea)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("更新产地 - 失败 - 无权限（采购方）")
    @WithMockUser(roles = {"PURCHASER"})
    void testUpdateOriginAreaFailureAsPurchaser() throws Exception {
        OriginArea originArea = new OriginArea();
        originArea.setAreaId(1);
        originArea.setAreaCode("510124");
        originArea.setAreaName("更新后的产地2");
        originArea.setProvince("四川省");
        originArea.setCity("成都市");

        mockMvc.perform(put("/api/shared/origin-area")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(originArea)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("删除产地 - 成功")
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteOriginAreaSuccess() throws Exception {
        mockMvc.perform(delete("/api/shared/origin-area/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("删除产地 - 失败 - 无权限（未登录）")
    void testDeleteOriginAreaFailureWithoutLogin() throws Exception {
        mockMvc.perform(delete("/api/shared/origin-area/1"))
                .andExpect(status().isUnauthorized());
    }
}