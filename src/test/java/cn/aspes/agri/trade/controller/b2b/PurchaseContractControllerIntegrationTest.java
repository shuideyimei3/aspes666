package cn.aspes.agri.trade.controller.b2b;

import cn.aspes.agri.trade.dto.ContractRequest;
import cn.aspes.agri.trade.entity.PurchaseContract;
import cn.aspes.agri.trade.security.CustomUserDetails;
import cn.aspes.agri.trade.service.PurchaseContractService;
import cn.aspes.agri.trade.vo.PurchaseContractVO;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PurchaseContractController.class)
public class PurchaseContractControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PurchaseContractService purchaseContractService;

    @BeforeEach
    void setUp() {
        // 模拟创建合同返回值
        when(purchaseContractService.createContract(anyLong(), any(ContractRequest.class)))
                .thenReturn(1L);

        // 模拟查询我的合同列表
        IPage<PurchaseContract> contractPage = new IPage<PurchaseContract>() {
            @Override
            public java.util.List<PurchaseContract> getRecords() {
                return java.util.Collections.emptyList();
            }

            @Override
            public IPage<PurchaseContract> setRecords(java.util.List<PurchaseContract> records) {
                return this;
            }

            @Override
            public long getTotal() {
                return 0;
            }

            @Override
            public IPage<PurchaseContract> setTotal(long total) {
                return this;
            }

            @Override
            public long getSize() {
                return 10;
            }

            @Override
            public IPage<PurchaseContract> setSize(long size) {
                return this;
            }

            @Override
            public long getCurrent() {
                return 1;
            }

            @Override
            public IPage<PurchaseContract> setCurrent(long current) {
                return this;
            }

            @Override
            public long getPages() {
                return 0;
            }
        };
        when(purchaseContractService.listMyContracts(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(contractPage);

        // 模拟获取合同详情
        PurchaseContract contract = new PurchaseContract();
        contract.setId(1L);
        when(purchaseContractService.getById(1L))
                .thenReturn(contract);
        when(purchaseContractService.getById(99999L))
                .thenReturn(null);
    }

    @Test
    @DisplayName("创建合同 - 成功")
    @WithMockUser(roles = {"PURCHASER"})
    void testCreateContractSuccess() throws Exception {
        MockMultipartFile signFile = new MockMultipartFile(
                "signFile", "contract.pdf", "application/pdf", "contract content".getBytes());

        mockMvc.perform(multipart("/api/b2b/contracts")
                        .file(signFile)
                        .param("farmerId", "1")
                        .param("purchaserId", "1")
                        .param("productId", "1")
                        .param("quantity", "100")
                        .param("unit", "斤")
                        .param("price", "2.50")
                        .param("totalAmount", "250.00")
                        .param("deliveryDate", LocalDate.now().plusDays(7).toString())
                        .param("deliveryAddress", "四川省成都市")
                        .param("paymentTerms", "货到付款")
                        .param("qualityStandard", "新鲜有机蔬菜"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("签署合同 - 成功")
    @WithMockUser(roles = {"FARMER"})
    void testSignContractSuccess() throws Exception {
        MockMultipartFile signFile = new MockMultipartFile(
                "signFile", "signature.png", "image/png", "signature content".getBytes());

        mockMvc.perform(multipart("/api/b2b/contracts/1/sign")
                        .file(signFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("撤回合同 - 成功")
    @WithMockUser(roles = {"PURCHASER"})
    void testWithdrawContractSuccess() throws Exception {
        mockMvc.perform(put("/api/b2b/contracts/1/withdraw")
                        .param("reason", "信息有误"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("拒签合同 - 成功")
    @WithMockUser(roles = {"FARMER"})
    void testRejectContractSuccess() throws Exception {
        mockMvc.perform(put("/api/b2b/contracts/1/reject")
                        .param("reason", "不符合要求"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("终止合同 - 成功")
    @WithMockUser(roles = {"PURCHASER"})
    void testTerminateContractSuccess() throws Exception {
        mockMvc.perform(put("/api/b2b/contracts/1/terminate")
                        .param("reason", "双方协商一致"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("查询我的合同列表 - 成功 - 采购方")
    @WithMockUser(roles = {"PURCHASER"})
    void testListMyContractsPurchaserSuccess() throws Exception {
        mockMvc.perform(get("/api/b2b/contracts/my")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("查询我的合同列表 - 成功 - 农户")
    @WithMockUser(roles = {"FARMER"})
    void testListMyContractsFarmerSuccess() throws Exception {
        mockMvc.perform(get("/api/b2b/contracts/my")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("查询合同详情 - 成功")
    void testGetContractSuccess() throws Exception {
        mockMvc.perform(get("/api/b2b/contracts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("查询合同详情 - 失败 - 合同不存在")
    void testGetContractFailureWithNonexistentContract() throws Exception {
        mockMvc.perform(get("/api/b2b/contracts/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}