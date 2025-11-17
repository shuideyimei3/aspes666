package cn.aspes.agri.trade.controller.b2c;

import cn.aspes.agri.trade.dto.FarmerInfoRequest;
import cn.aspes.agri.trade.entity.FarmerInfo;
import cn.aspes.agri.trade.service.FarmerInfoService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FarmerInfoController.class)
public class FarmerInfoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FarmerInfoService farmerInfoService;

    @BeforeEach
    void setUp() {
        // Mock farmer info service to return a valid farmer info
        FarmerInfo mockFarmerInfo = new FarmerInfo();
        mockFarmerInfo.setId(1L);
        mockFarmerInfo.setName("测试农户");
        mockFarmerInfo.setPhone("13800138000");
        mockFarmerInfo.setAddress("四川省成都市");
        mockFarmerInfo.setDescription("专业种植有机蔬菜");
        
        when(farmerInfoService.getById(1L)).thenReturn(mockFarmerInfo);
        when(farmerInfoService.getByUserId(anyLong())).thenReturn(mockFarmerInfo);
        when(farmerInfoService.getById(99999L)).thenReturn(null);
    }

    @Test
    @DisplayName("获取农户详情 - 成功")
    void testGetFarmerInfoSuccess() throws Exception {
        mockMvc.perform(get("/api/c2c/farmer-info/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试农户"))
                .andExpect(jsonPath("$.data.phone").value("13800138000"));
    }

    @Test
    @DisplayName("获取农户详情 - 失败 - 农户不存在")
    void testGetFarmerInfoFailureWithNonexistentFarmer() throws Exception {
        mockMvc.perform(get("/api/c2c/farmer-info/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("获取当前用户农户信息 - 成功")
    @WithMockUser(roles = {"FARMER"})
    void testGetCurrentFarmerInfoSuccess() throws Exception {
        mockMvc.perform(get("/api/c2c/farmer-info/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.name").value("测试农户"))
                .andExpect(jsonPath("$.data.phone").value("13800138000"));
    }

    @Test
    @DisplayName("获取当前用户农户信息 - 失败 - 无权限")
    @WithMockUser(roles = {"PURCHASER"})
    void testGetCurrentFarmerInfoFailureWithoutPermission() throws Exception {
        mockMvc.perform(get("/api/c2c/farmer-info/my"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("获取当前用户农户信息 - 失败 - 未认证")
    void testGetCurrentFarmerInfoFailureWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/c2c/farmer-info/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("提交农户信息 - 成功")
    @WithMockUser(roles = {"FARMER"})
    void testSubmitFarmerInfoSuccess() throws Exception {
        // Mock service to do nothing
        doNothing().when(farmerInfoService).submitFarmerInfo(anyLong(), any(FarmerInfoRequest.class));

        FarmerInfoRequest request = new FarmerInfoRequest();
        request.setName("测试农户");
        request.setPhone("13800138000");
        request.setAddress("四川省成都市");
        request.setDescription("专业种植有机蔬菜");
        request.setBusinessLicense("91510100MA61R7LXX1");
        request.setBankAccount("6222021234567890123");
        request.setBankName("中国工商银行");

        MockMultipartFile idCardFrontFile = new MockMultipartFile(
                "idCardFrontFile", 
                "front.jpg", 
                MediaType.IMAGE_JPEG_VALUE, 
                "front card image".getBytes());

        MockMultipartFile idCardBackFile = new MockMultipartFile(
                "idCardBackFile", 
                "back.jpg", 
                MediaType.IMAGE_JPEG_VALUE, 
                "back card image".getBytes());

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", 
                "", 
                MediaType.APPLICATION_JSON_VALUE, 
                objectMapper.writeValueAsString(request).getBytes());

        mockMvc.perform(multipart("/api/c2c/farmer-info")
                        .file(idCardFrontFile)
                        .file(idCardBackFile)
                        .file(requestPart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("提交农户信息 - 失败 - 无权限")
    @WithMockUser(roles = {"PURCHASER"})
    void testSubmitFarmerInfoFailureWithoutPermission() throws Exception {
        FarmerInfoRequest request = new FarmerInfoRequest();
        request.setName("测试农户");
        request.setPhone("13800138000");
        request.setAddress("四川省成都市");

        MockMultipartFile idCardFrontFile = new MockMultipartFile(
                "idCardFrontFile", 
                "front.jpg", 
                MediaType.IMAGE_JPEG_VALUE, 
                "front card image".getBytes());

        MockMultipartFile idCardBackFile = new MockMultipartFile(
                "idCardBackFile", 
                "back.jpg", 
                MediaType.IMAGE_JPEG_VALUE, 
                "back card image".getBytes());

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", 
                "", 
                MediaType.APPLICATION_JSON_VALUE, 
                objectMapper.writeValueAsString(request).getBytes());

        mockMvc.perform(multipart("/api/c2c/farmer-info")
                        .file(idCardFrontFile)
                        .file(idCardBackFile)
                        .file(requestPart))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("提交农户信息 - 失败 - 未认证")
    void testSubmitFarmerInfoFailureWithoutAuthentication() throws Exception {
        FarmerInfoRequest request = new FarmerInfoRequest();
        request.setName("测试农户");
        request.setPhone("13800138000");
        request.setAddress("四川省成都市");

        MockMultipartFile idCardFrontFile = new MockMultipartFile(
                "idCardFrontFile", 
                "front.jpg", 
                MediaType.IMAGE_JPEG_VALUE, 
                "front card image".getBytes());

        MockMultipartFile idCardBackFile = new MockMultipartFile(
                "idCardBackFile", 
                "back.jpg", 
                MediaType.IMAGE_JPEG_VALUE, 
                "back card image".getBytes());

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", 
                "", 
                MediaType.APPLICATION_JSON_VALUE, 
                objectMapper.writeValueAsString(request).getBytes());

        mockMvc.perform(multipart("/api/c2c/farmer-info")
                        .file(idCardFrontFile)
                        .file(idCardBackFile)
                        .file(requestPart))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("修改农户信息 - 成功")
    @WithMockUser(roles = {"FARMER"})
    void testUpdateFarmerInfoSuccess() throws Exception {
        // Mock service to do nothing
        doNothing().when(farmerInfoService).updateFarmerInfo(anyLong(), anyLong(), any(FarmerInfoRequest.class));

        FarmerInfoRequest request = new FarmerInfoRequest();
        request.setName("更新后的农户");
        request.setPhone("13800138001");
        request.setAddress("四川省成都市更新地址");
        request.setDescription("更新后的描述");
        request.setBusinessLicense("91510100MA61R7LXX2");
        request.setBankAccount("6222021234567890124");
        request.setBankName("中国农业银行");

        mockMvc.perform(put("/api/c2c/farmer-info/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("修改农户信息 - 失败 - 无权限")
    @WithMockUser(roles = {"PURCHASER"})
    void testUpdateFarmerInfoFailureWithoutPermission() throws Exception {
        FarmerInfoRequest request = new FarmerInfoRequest();
        request.setName("更新后的农户");
        request.setPhone("13800138001");
        request.setAddress("四川省成都市更新地址");

        mockMvc.perform(put("/api/c2c/farmer-info/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("修改农户信息 - 失败 - 未认证")
    void testUpdateFarmerInfoFailureWithoutAuthentication() throws Exception {
        FarmerInfoRequest request = new FarmerInfoRequest();
        request.setName("更新后的农户");
        request.setPhone("13800138001");
        request.setAddress("四川省成都市更新地址");

        mockMvc.perform(put("/api/c2c/farmer-info/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}