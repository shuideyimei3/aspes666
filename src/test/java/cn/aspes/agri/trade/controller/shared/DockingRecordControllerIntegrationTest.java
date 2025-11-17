package cn.aspes.agri.trade.controller.shared;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.converter.EntityVOConverter;
import cn.aspes.agri.trade.dto.DockingFeedbackRequest;
import cn.aspes.agri.trade.dto.DockingRecordRequest;
import cn.aspes.agri.trade.entity.DockingRecord;
import cn.aspes.agri.trade.security.CustomUserDetails;
import cn.aspes.agri.trade.service.DockingRecordService;
import cn.aspes.agri.trade.service.FarmerInfoService;
import cn.aspes.agri.trade.service.PurchaserInfoService;
import cn.aspes.agri.trade.vo.DockingRecordVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DockingRecordController.class)
@DisplayName("共享 - 对接记录管理控制器集成测试")
class DockingRecordControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DockingRecordService dockingRecordService;

    @MockBean
    private FarmerInfoService farmerInfoService;

    @MockBean
    private PurchaserInfoService purchaserInfoService;

    @MockBean
    private EntityVOConverter entityVOConverter;

    @Autowired
    private ObjectMapper objectMapper;

    private DockingRecord mockDockingRecord;
    private DockingRecordVO mockDockingRecordVO;
    private IPage<DockingRecord> mockPage;
    private IPage<DockingRecordVO> mockVOPage;
    private CustomUserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        // 创建模拟的对接记录
        mockDockingRecord = new DockingRecord();
        mockDockingRecord.setId(1L);
        mockDockingRecord.setDemandId(1L);
        mockDockingRecord.setFarmerId(1L);
        mockDockingRecord.setProductId(1L);
        mockDockingRecord.setQuantity(50);
        mockDockingRecord.setUnit("斤");
        mockDockingRecord.setPrice(new BigDecimal("2.50"));
        mockDockingRecord.setRemark("新鲜有机蔬菜，质量保证");

        // 创建模拟的对接记录VO
        mockDockingRecordVO = new DockingRecordVO();
        mockDockingRecordVO.setId(1L);
        mockDockingRecordVO.setDemandId(1L);
        mockDockingRecordVO.setFarmerId(1L);
        mockDockingRecordVO.setProductId(1L);
        mockDockingRecordVO.setQuantity(50);
        mockDockingRecordVO.setUnit("斤");
        mockDockingRecordVO.setPrice(new BigDecimal("2.50"));
        mockDockingRecordVO.setRemark("新鲜有机蔬菜，质量保证");

        // 创建模拟的分页对象
        List<DockingRecord> recordList = new ArrayList<>();
        recordList.add(mockDockingRecord);
        mockPage = new Page<>(1, 10);
        ((Page<DockingRecord>) mockPage).setRecords(recordList);
        ((Page<DockingRecord>) mockPage).setTotal(1);

        // 创建模拟的VO分页对象
        List<DockingRecordVO> voList = new ArrayList<>();
        voList.add(mockDockingRecordVO);
        mockVOPage = new Page<>(1, 10);
        ((Page<DockingRecordVO>) mockVOPage).setRecords(voList);
        ((Page<DockingRecordVO>) mockVOPage).setTotal(1);

        // 创建模拟的用户详情
        mockUserDetails = new CustomUserDetails();
        mockUserDetails.setId(1L);
    }

    @Test
    @WithMockUser(roles = {"FARMER"})
    @DisplayName("农户响应需求 - 成功")
    void respondToDemand_Farmer_Success() throws Exception {
        DockingRecordRequest request = new DockingRecordRequest();
        request.setDemandId(1L);
        request.setProductId(1L);
        request.setQuantity(50);
        request.setUnit("斤");
        request.setPrice(new BigDecimal("2.50"));
        request.setRemark("新鲜有机蔬菜，质量保证");

        when(dockingRecordService.respondToDemand(anyLong(), any(DockingRecordRequest.class))).thenReturn(1L);

        mockMvc.perform(post("/api/shared/dockings/respond")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @WithMockUser(roles = {"PURCHASER"})
    @DisplayName("农户响应需求 - 无权限")
    void respondToDemand_Purchaser_Forbidden() throws Exception {
        DockingRecordRequest request = new DockingRecordRequest();
        request.setDemandId(1L);
        request.setProductId(1L);
        request.setQuantity(50);
        request.setUnit("斤");
        request.setPrice(new BigDecimal("2.50"));

        mockMvc.perform(post("/api/shared/dockings/respond")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"PURCHASER"})
    @DisplayName("采购方处理对接 - 成功 - 接受")
    void handleDocking_Purchaser_Accept_Success() throws Exception {
        DockingFeedbackRequest request = new DockingFeedbackRequest();
        request.setAction("accept");

        doNothing().when(dockingRecordService).handleDocking(anyLong(), anyLong(), any(DockingFeedbackRequest.class));

        mockMvc.perform(put("/api/shared/dockings/1/handle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"PURCHASER"})
    @DisplayName("采购方处理对接 - 成功 - 拒绝")
    void handleDocking_Purchaser_Reject_Success() throws Exception {
        DockingFeedbackRequest request = new DockingFeedbackRequest();
        request.setAction("reject");
        request.setRemark("不符合质量要求");

        doNothing().when(dockingRecordService).handleDocking(anyLong(), anyLong(), any(DockingFeedbackRequest.class));

        mockMvc.perform(put("/api/shared/dockings/1/handle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"FARMER"})
    @DisplayName("采购方处理对接 - 无权限")
    void handleDocking_Farmer_Forbidden() throws Exception {
        DockingFeedbackRequest request = new DockingFeedbackRequest();
        request.setAction("accept");

        mockMvc.perform(put("/api/shared/dockings/1/handle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("查询需求的对接列表 - 成功")
    void listByDemand_Success() throws Exception {
        when(dockingRecordService.listByDemand(anyLong(), anyInt(), anyInt())).thenReturn(mockPage);
        when(entityVOConverter.toDockingRecordVOPage(any(IPage.class))).thenReturn(mockVOPage);

        mockMvc.perform(get("/api/shared/dockings/demand/1")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @WithMockUser(roles = {"FARMER"})
    @DisplayName("查询我的对接记录 - 成功 - 农户")
    void listMyDockings_Farmer_Success() throws Exception {
        when(dockingRecordService.listMyDockings(anyLong(), anyString(), anyInt(), anyInt())).thenReturn(mockPage);
        when(entityVOConverter.toDockingRecordVOPage(any(IPage.class))).thenReturn(mockVOPage);

        mockMvc.perform(get("/api/shared/dockings/my")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @WithMockUser(roles = {"PURCHASER"})
    @DisplayName("查询我的对接记录 - 成功 - 采购方")
    void listMyDockings_Purchaser_Success() throws Exception {
        when(dockingRecordService.listMyDockings(anyLong(), anyString(), anyInt(), anyInt())).thenReturn(mockPage);
        when(entityVOConverter.toDockingRecordVOPage(any(IPage.class))).thenReturn(mockVOPage);

        mockMvc.perform(get("/api/shared/dockings/my")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("查询我的对接记录 - 未认证")
    void listMyDockings_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/shared/dockings/my")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isUnauthorized());
    }
}