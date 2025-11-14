package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.dto.UserCertificationRequest;
import cn.aspes.agri.trade.entity.User;
import cn.aspes.agri.trade.entity.UserCertificationApply;
import cn.aspes.agri.trade.enums.UserRole;
import cn.aspes.agri.trade.exception.BusinessException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户认证服务集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserCertificationServiceIntegrationTest {

    @Autowired
    private UserCertificationService userCertificationService;
    
    @Autowired
    private UserService userService;
    
    private User farmerUser;
    private User purchaserUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        // 创建农户用户
        cn.aspes.agri.trade.dto.UserRegisterRequest farmerRequest = new cn.aspes.agri.trade.dto.UserRegisterRequest();
        farmerRequest.setUsername("farmer_cert" + System.currentTimeMillis());
        farmerRequest.setPassword("password123");
        farmerRequest.setRole(UserRole.FARMER);
        farmerRequest.setContactPerson("农户认证测试");
        farmerRequest.setContactPhone("13800138001");
        farmerRequest.setContactEmail("farmer_cert@example.com");
        Long farmerUserId = userService.register(farmerRequest);
        farmerUser = userService.getById(farmerUserId);
        
        // 创建采购方用户
        cn.aspes.agri.trade.dto.UserRegisterRequest purchaserRequest = new cn.aspes.agri.trade.dto.UserRegisterRequest();
        purchaserRequest.setUsername("purchaser_cert" + System.currentTimeMillis());
        purchaserRequest.setPassword("password123");
        purchaserRequest.setRole(UserRole.PURCHASER);
        purchaserRequest.setContactPerson("采购方认证测试");
        purchaserRequest.setContactPhone("13800138002");
        purchaserRequest.setContactEmail("purchaser_cert@example.com");
        Long purchaserUserId = userService.register(purchaserRequest);
        purchaserUser = userService.getById(purchaserUserId);
        
        // 创建管理员用户
        cn.aspes.agri.trade.dto.UserRegisterRequest adminRequest = new cn.aspes.agri.trade.dto.UserRegisterRequest();
        adminRequest.setUsername("admin_cert" + System.currentTimeMillis());
        adminRequest.setPassword("password123");
        adminRequest.setRole(UserRole.ADMIN);
        adminRequest.setContactPerson("管理员认证测试");
        adminRequest.setContactPhone("13800138003");
        adminRequest.setContactEmail("admin_cert@example.com");
        Long adminUserId = userService.register(adminRequest);
        adminUser = userService.getById(adminUserId);
    }

    @Test
    @DisplayName("农户提交认证申请")
    void testSubmitFarmerCertification() {
        UserCertificationRequest request = new UserCertificationRequest();
        request.setApplyType("farmer");
        request.setIdNumber("123456789012345678");
        request.setIdCardFrontUrl("https://example.com/id-front.jpg");
        request.setIdCardBackUrl("https://example.com/id-back.jpg");
        request.setApplyReason("申请农户认证");

        Long applyId = userCertificationService.submitCertification(farmerUser.getId(), request);
        
        assertNotNull(applyId);
        assertTrue(applyId > 0);
        
        // 验证认证申请已创建
        UserCertificationApply apply = userCertificationService.getById(applyId);
        assertNotNull(apply);
        assertEquals(farmerUser.getId(), apply.getUserId());
        assertEquals("farmer", apply.getApplyType());
        assertEquals("pending", apply.getStatus());
        assertEquals("123456789012345678", apply.getIdNumber());
        assertEquals("https://example.com/id-front.jpg", apply.getIdCardFrontUrl());
        assertEquals("https://example.com/id-back.jpg", apply.getIdCardBackUrl());
        assertEquals("申请农户认证", apply.getApplyReason());
    }

    @Test
    @DisplayName("采购方提交认证申请")
    void testSubmitPurchaserCertification() {
        UserCertificationRequest request = new UserCertificationRequest();
        request.setApplyType("purchaser");
        request.setIdNumber("91000000000000000X");
        request.setBusinessLicenseUrl("https://example.com/license.jpg");
        request.setLegalRepresentative("法定代表人");
        request.setApplyReason("申请采购方认证");

        Long applyId = userCertificationService.submitCertification(purchaserUser.getId(), request);
        
        assertNotNull(applyId);
        assertTrue(applyId > 0);
        
        // 验证认证申请已创建
        UserCertificationApply apply = userCertificationService.getById(applyId);
        assertNotNull(apply);
        assertEquals(purchaserUser.getId(), apply.getUserId());
        assertEquals("purchaser", apply.getApplyType());
        assertEquals("pending", apply.getStatus());
        assertEquals("91000000000000000X", apply.getIdNumber());
        assertEquals("https://example.com/license.jpg", apply.getBusinessLicenseUrl());
        assertEquals("法定代表人", apply.getLegalRepresentative());
        assertEquals("申请采购方认证", apply.getApplyReason());
    }

    @Test
    @DisplayName("重复提交相同类型的认证申请应失败")
    void testDuplicateCertificationApplication() {
        // 第一次提交
        UserCertificationRequest request = new UserCertificationRequest();
        request.setApplyType("farmer");
        request.setIdNumber("123456789012345678");
        request.setIdCardFrontUrl("https://example.com/id-front.jpg");
        request.setIdCardBackUrl("https://example.com/id-back.jpg");
        request.setApplyReason("申请农户认证");

        userCertificationService.submitCertification(farmerUser.getId(), request);
        
        // 第二次提交相同类型应失败
        assertThrows(BusinessException.class, () -> {
            userCertificationService.submitCertification(farmerUser.getId(), request);
        });
    }

    @Test
    @DisplayName("获取用户的认证申请状态")
    void testGetUserCertification() {
        // 提交认证申请
        UserCertificationRequest request = new UserCertificationRequest();
        request.setApplyType("farmer");
        request.setIdNumber("123456789012345678");
        request.setIdCardFrontUrl("https://example.com/id-front.jpg");
        request.setIdCardBackUrl("https://example.com/id-back.jpg");
        request.setApplyReason("申请农户认证");

        userCertificationService.submitCertification(farmerUser.getId(), request);
        
        // 获取认证申请
        UserCertificationApply apply = userCertificationService.getUserCertification(farmerUser.getId(), "farmer");
        
        assertNotNull(apply);
        assertEquals(farmerUser.getId(), apply.getUserId());
        assertEquals("farmer", apply.getApplyType());
        assertEquals("pending", apply.getStatus());
    }

    @Test
    @DisplayName("管理员批准认证申请")
    void testApproveCertification() {
        // 提交认证申请
        UserCertificationRequest request = new UserCertificationRequest();
        request.setApplyType("farmer");
        request.setIdNumber("123456789012345678");
        request.setIdCardFrontUrl("https://example.com/id-front.jpg");
        request.setIdCardBackUrl("https://example.com/id-back.jpg");
        request.setApplyReason("申请农户认证");

        Long applyId = userCertificationService.submitCertification(farmerUser.getId(), request);
        
        // 批准认证申请
        userCertificationService.approveCertification(applyId, "认证信息完整，批准通过");
        
        // 验证申请状态已更新
        UserCertificationApply apply = userCertificationService.getById(applyId);
        assertEquals("approved", apply.getStatus());
        assertEquals("认证信息完整，批准通过", apply.getAdminRemark());
        assertNotNull(apply.getApprovedTime());
        
        // 验证用户认证状态已更新
        User updatedUser = userService.getById(farmerUser.getId());
        assertEquals(1, updatedUser.getIsCertified());
    }

    @Test
    @DisplayName("管理员拒绝认证申请")
    void testRejectCertification() {
        // 提交认证申请
        UserCertificationRequest request = new UserCertificationRequest();
        request.setApplyType("purchaser");
        request.setIdNumber("91000000000000000X");
        request.setBusinessLicenseUrl("https://example.com/license.jpg");
        request.setLegalRepresentative("法定代表人");
        request.setApplyReason("申请采购方认证");

        Long applyId = userCertificationService.submitCertification(purchaserUser.getId(), request);
        
        // 拒绝认证申请
        userCertificationService.rejectCertification(applyId, "营业执照不清晰，请重新提交");
        
        // 验证申请状态已更新
        UserCertificationApply apply = userCertificationService.getById(applyId);
        assertEquals("rejected", apply.getStatus());
        assertEquals("营业执照不清晰，请重新提交", apply.getAdminRemark());
        assertNull(apply.getApprovedTime());
        
        // 验证用户认证状态未更新
        User updatedUser = userService.getById(purchaserUser.getId());
        assertEquals(0, updatedUser.getIsCertified());
    }

    @Test
    @DisplayName("重复处理已批准的认证申请应失败")
    void testDuplicateApproveCertification() {
        // 提交认证申请
        UserCertificationRequest request = new UserCertificationRequest();
        request.setApplyType("farmer");
        request.setIdNumber("123456789012345678");
        request.setIdCardFrontUrl("https://example.com/id-front.jpg");
        request.setIdCardBackUrl("https://example.com/id-back.jpg");
        request.setApplyReason("申请农户认证");

        Long applyId = userCertificationService.submitCertification(farmerUser.getId(), request);
        
        // 批准认证申请
        userCertificationService.approveCertification(applyId, "认证信息完整，批准通过");
        
        // 再次批准应失败
        assertThrows(BusinessException.class, () -> {
            userCertificationService.approveCertification(applyId, "重复批准");
        });
    }

    @Test
    @DisplayName("分页查询待审核的认证申请")
    void testPagePendingApplications() {
        // 提交多个认证申请
        for (int i = 0; i < 5; i++) {
            UserCertificationRequest request = new UserCertificationRequest();
            request.setApplyType(i % 2 == 0 ? "farmer" : "purchaser");
            request.setIdNumber("12345678901234567" + i);
            request.setIdCardFrontUrl("https://example.com/id-front" + i + ".jpg");
            request.setIdCardBackUrl("https://example.com/id-back" + i + ".jpg");
            request.setApplyReason("申请认证" + i);

            userCertificationService.submitCertification(farmerUser.getId(), request);
        }
        
        // 查询所有待审核申请
        Page<UserCertificationApply> page = userCertificationService.pagePendingApplications(1, 10, null);
        assertTrue(page.getRecords().size() >= 5);
        
        // 查询农户类型待审核申请
        Page<UserCertificationApply> farmerPage = userCertificationService.pagePendingApplications(1, 10, "farmer");
        assertTrue(farmerPage.getRecords().size() >= 3);
        
        // 查询采购方类型待审核申请
        Page<UserCertificationApply> purchaserPage = userCertificationService.pagePendingApplications(1, 10, "purchaser");
        assertTrue(purchaserPage.getRecords().size() >= 2);
    }

    @Test
    @DisplayName("分页查询所有认证申请")
    void testPageApplications() {
        // 提交多个认证申请
        UserCertificationRequest request1 = new UserCertificationRequest();
        request1.setApplyType("farmer");
        request1.setIdNumber("123456789012345678");
        request1.setIdCardFrontUrl("https://example.com/id-front.jpg");
        request1.setIdCardBackUrl("https://example.com/id-back.jpg");
        request1.setApplyReason("申请农户认证");
        Long applyId1 = userCertificationService.submitCertification(farmerUser.getId(), request1);
        
        UserCertificationRequest request2 = new UserCertificationRequest();
        request2.setApplyType("purchaser");
        request2.setIdNumber("91000000000000000X");
        request2.setBusinessLicenseUrl("https://example.com/license.jpg");
        request2.setLegalRepresentative("法定代表人");
        request2.setApplyReason("申请采购方认证");
        Long applyId2 = userCertificationService.submitCertification(purchaserUser.getId(), request2);
        
        // 批准第一个申请
        userCertificationService.approveCertification(applyId1, "认证信息完整，批准通过");
        
        // 查询所有申请
        Page<UserCertificationApply> allPage = userCertificationService.pageApplications(1, 10, null);
        assertTrue(allPage.getRecords().size() >= 2);
        
        // 查询待审核申请
        Page<UserCertificationApply> pendingPage = userCertificationService.pageApplications(1, 10, "pending");
        assertTrue(pendingPage.getRecords().size() >= 1);
        
        // 查询已批准申请
        Page<UserCertificationApply> approvedPage = userCertificationService.pageApplications(1, 10, "approved");
        assertTrue(approvedPage.getRecords().size() >= 1);
    }

    @Test
    @DisplayName("不存在的用户提交认证申请应失败")
    void testCertificationWithNonExistentUser() {
        UserCertificationRequest request = new UserCertificationRequest();
        request.setApplyType("farmer");
        request.setIdNumber("123456789012345678");
        request.setIdCardFrontUrl("https://example.com/id-front.jpg");
        request.setIdCardBackUrl("https://example.com/id-back.jpg");
        request.setApplyReason("申请农户认证");

        assertThrows(BusinessException.class, () -> {
            userCertificationService.submitCertification(999999L, request);
        });
    }

    @Test
    @DisplayName("处理不存在的认证申请应失败")
    void testProcessNonExistentCertification() {
        assertThrows(BusinessException.class, () -> {
            userCertificationService.approveCertification(999999L, "批准");
        });
        
        assertThrows(BusinessException.class, () -> {
            userCertificationService.rejectCertification(999999L, "拒绝");
        });
    }
}