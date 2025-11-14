package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.dto.UserRegisterRequest;
import cn.aspes.agri.trade.entity.*;
import cn.aspes.agri.trade.enums.ContractStatus;
import cn.aspes.agri.trade.enums.OrderStatus;
import cn.aspes.agri.trade.enums.ProductStatus;
import cn.aspes.agri.trade.enums.UserRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 合作评价服务集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CooperationReviewServiceIntegrationTest {

    @Autowired
    private CooperationReviewService cooperationReviewService;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private PurchaseContractService purchaseContractService;

    @Autowired
    private FarmerProductService farmerProductService;

    @Autowired
    private FarmerInfoService farmerInfoService;

    @Autowired
    private PurchaserInfoService purchaserInfoService;

    @Autowired
    private UserService userService;

    private Long farmerId;
    private Long purchaserId;
    private Long farmerInfoId;
    private Long purchaserInfoId;
    private Long orderId;

    @BeforeEach
    void setUp() {
        // 创建农民用户
        UserRegisterRequest farmerRequest = new UserRegisterRequest();
        farmerRequest.setUsername("farmer_review" + System.currentTimeMillis());
        farmerRequest.setPassword("password123");
        farmerRequest.setRole(UserRole.FARMER);
        farmerRequest.setContactPerson("张三");
        farmerRequest.setContactPhone("13800138000");
        farmerRequest.setContactEmail("farmer@example.com");
        farmerId = userService.register(farmerRequest);

        // 创建采购方用户
        UserRegisterRequest purchaserRequest = new UserRegisterRequest();
        purchaserRequest.setUsername("purchaser_review" + System.currentTimeMillis());
        purchaserRequest.setPassword("password123");
        purchaserRequest.setRole(UserRole.PURCHASER);
        purchaserRequest.setContactPerson("李四");
        purchaserRequest.setContactPhone("13800138001");
        purchaserRequest.setContactEmail("purchaser@example.com");
        purchaserId = userService.register(purchaserRequest);

        // 创建农民信息
        FarmerInfo farmerInfo = new FarmerInfo();
        farmerInfo.setUserId(farmerId);
        farmerInfo.setFarmName("示范农场");
        farmerInfo.setOriginAreaId(1);
        farmerInfoService.save(farmerInfo);
        farmerInfoId = farmerInfo.getId();

        // 创建采购方信息
        PurchaserInfo purchaserInfo = new PurchaserInfo();
        purchaserInfo.setUserId(purchaserId);
        purchaserInfo.setCompanyName("测试采购公司");
        purchaserInfoService.save(purchaserInfo);
        purchaserInfoId = purchaserInfo.getId();

        // 创建产品
        FarmerProduct product = new FarmerProduct();
        product.setFarmerId(farmerId);
        product.setCategoryId(1L);
        product.setOriginAreaId(1);
        product.setName("水稻");
        product.setSpec("标准规格");
        product.setUnit("公斤");
        product.setPrice(new BigDecimal("5.00"));
        product.setMinPurchase(10);
        product.setStock(1000);
        product.setStatus(ProductStatus.ON_SALE);
        farmerProductService.save(product);

        // 创建采购合同
        PurchaseContract contract = new PurchaseContract();
        contract.setPurchaserId(purchaserId);
        contract.setFarmerId(farmerId);
        contract.setTotalAmount(new BigDecimal("500.00"));
        contract.setPaymentTerms("30天内支付");
        contract.setDeliveryTime(LocalDate.now().plusDays(7));
        contract.setDeliveryAddress("浙江省杭州市");
        contract.setStatus(ContractStatus.SIGNED);
        purchaseContractService.save(contract);

        // 创建订单
        purchaseOrderService.createOrderFromContract(contract.getId());
        Page<PurchaseOrder> page = purchaseOrderService.pageOrders(1, 10, null);
        orderId = page.getRecords().get(0).getId();

        // 完成订单
        purchaseOrderService.completeOrder(orderId);
    }

    @Test
    void testSubmitReview() {
        CooperationReview review = new CooperationReview();
        review.setOrderId(orderId);
        review.setReviewFrom(String.valueOf(purchaserId));
        review.setReviewTo(String.valueOf(farmerId));
        review.setTargetId(farmerInfoId);
        review.setRating(5);
        review.setComment("产品质量很好，合作愉快！");

        // 提交评价
        cooperationReviewService.submitReview(review);

        // 验证评价已保存
        CooperationReview savedReview = cooperationReviewService.getById(review.getId());
        assertNotNull(savedReview);
        assertEquals(orderId, savedReview.getOrderId());
        assertEquals(String.valueOf(purchaserId), savedReview.getReviewFrom());
        assertEquals(String.valueOf(farmerId), savedReview.getReviewTo());
        assertEquals(farmerInfoId, savedReview.getTargetId());
        assertEquals(5, savedReview.getRating());
        assertEquals("产品质量很好，合作愉快！", savedReview.getComment());
        assertNotNull(savedReview.getCreateTime());
    }

    @Test
    void testSubmitReviewWithInvalidRating() {
        CooperationReview review = new CooperationReview();
        review.setOrderId(orderId);
        review.setReviewFrom(String.valueOf(purchaserId));
        review.setReviewTo(String.valueOf(farmerId));
        review.setTargetId(farmerInfoId);
        review.setRating(6); // 无效评分
        review.setComment("测试评价");

        // 提交评价应该失败
        assertThrows(Exception.class, () -> cooperationReviewService.submitReview(review));
    }

    @Test
    void testSubmitReviewForNonCompletedOrder() {
        // 创建新订单但不完成
        PurchaseContract contract = new PurchaseContract();
        contract.setPurchaserId(purchaserId);
        contract.setFarmerId(farmerId);
        contract.setTotalAmount(new BigDecimal("300.00"));
        contract.setPaymentTerms("30天内支付");
        contract.setDeliveryTime(LocalDate.now().plusDays(7));
        contract.setDeliveryAddress("上海市");
        contract.setStatus(ContractStatus.SIGNED);
        purchaseContractService.save(contract);

        purchaseOrderService.createOrderFromContract(contract.getId());
        Page<PurchaseOrder> page = purchaseOrderService.pageOrders(1, 10, null);
        Long newOrderId = page.getRecords().get(0).getId();

        CooperationReview review = new CooperationReview();
        review.setOrderId(newOrderId);
        review.setReviewFrom(String.valueOf(purchaserId));
        review.setReviewTo(String.valueOf(farmerId));
        review.setTargetId(farmerInfoId);
        review.setRating(4);
        review.setComment("测试评价");

        // 提交评价应该失败，因为订单未完成
        assertThrows(Exception.class, () -> cooperationReviewService.submitReview(review));
    }

    @Test
    void testSubmitDuplicateReview() {
        // 先提交一次评价
        CooperationReview review1 = new CooperationReview();
        review1.setOrderId(orderId);
        review1.setReviewFrom(String.valueOf(purchaserId));
        review1.setReviewTo(String.valueOf(farmerId));
        review1.setTargetId(farmerInfoId);
        review1.setRating(5);
        review1.setComment("第一次评价");
        cooperationReviewService.submitReview(review1);

        // 尝试再次评价同一订单
        CooperationReview review2 = new CooperationReview();
        review2.setOrderId(orderId);
        review2.setReviewFrom(String.valueOf(purchaserId));
        review2.setReviewTo(String.valueOf(farmerId));
        review2.setTargetId(farmerInfoId);
        review2.setRating(4);
        review2.setComment("第二次评价");

        // 提交评价应该失败，因为已经评价过
        assertThrows(Exception.class, () -> cooperationReviewService.submitReview(review2));
    }

    @Test
    void testListMyReviews() {
        // 创建多个订单并评价
        for (int i = 0; i < 3; i++) {
            PurchaseContract contract = new PurchaseContract();
            contract.setPurchaserId(purchaserId);
            contract.setFarmerId(farmerId);
            contract.setTotalAmount(new BigDecimal("100.00"));
            contract.setPaymentTerms("30天内支付");
            contract.setDeliveryTime(LocalDate.now().plusDays(7));
            contract.setDeliveryAddress("测试地址" + i);
            contract.setStatus(ContractStatus.SIGNED);
            purchaseContractService.save(contract);

            purchaseOrderService.createOrderFromContract(contract.getId());
            Page<PurchaseOrder> page = purchaseOrderService.pageOrders(1, 10, null);
            Long newOrderId = page.getRecords().get(0).getId();
            purchaseOrderService.completeOrder(newOrderId);

            CooperationReview review = new CooperationReview();
            review.setOrderId(newOrderId);
            review.setReviewFrom(String.valueOf(purchaserId));
            review.setReviewTo(String.valueOf(farmerId));
            review.setTargetId(farmerInfoId);
            review.setRating(4 + i % 2);
            review.setComment("评价内容" + i);
            cooperationReviewService.submitReview(review);
        }

        // 查询我发表的评价
        Page<CooperationReview> myReviews = cooperationReviewService.listMyReviews(purchaserId, 1, 10);
        assertEquals(3, myReviews.getTotal());
        assertEquals(3, myReviews.getRecords().size());
    }

    @Test
    void testListReceivedReviews() {
        // 创建多个订单并评价
        for (int i = 0; i < 3; i++) {
            PurchaseContract contract = new PurchaseContract();
            contract.setPurchaserId(purchaserId);
            contract.setFarmerId(farmerId);
            contract.setTotalAmount(new BigDecimal("100.00"));
            contract.setPaymentTerms("30天内支付");
            contract.setDeliveryTime(LocalDate.now().plusDays(7));
            contract.setDeliveryAddress("测试地址" + i);
            contract.setStatus(ContractStatus.SIGNED);
            purchaseContractService.save(contract);

            purchaseOrderService.createOrderFromContract(contract.getId());
            Page<PurchaseOrder> page = purchaseOrderService.pageOrders(1, 10, null);
            Long newOrderId = page.getRecords().get(0).getId();
            purchaseOrderService.completeOrder(newOrderId);

            CooperationReview review = new CooperationReview();
            review.setOrderId(newOrderId);
            review.setReviewFrom(String.valueOf(purchaserId));
            review.setReviewTo(String.valueOf(farmerId));
            review.setTargetId(farmerInfoId);
            review.setRating(4 + i % 2);
            review.setComment("评价内容" + i);
            cooperationReviewService.submitReview(review);
        }

        // 查询收到的评价
        Page<CooperationReview> receivedReviews = cooperationReviewService.listReceivedReviews(farmerInfoId, 1, 10);
        assertEquals(3, receivedReviews.getTotal());
        assertEquals(3, receivedReviews.getRecords().size());
    }

    @Test
    void testUpdateReview() {
        // 提交评价
        CooperationReview review = new CooperationReview();
        review.setOrderId(orderId);
        review.setReviewFrom(String.valueOf(purchaserId));
        review.setReviewTo(String.valueOf(farmerId));
        review.setTargetId(farmerInfoId);
        review.setRating(3);
        review.setComment("原始评价");
        cooperationReviewService.submitReview(review);

        // 更新评价
        cooperationReviewService.updateReview(review.getId(), 5, "更新后的评价");

        // 验证评价已更新
        CooperationReview updatedReview = cooperationReviewService.getById(review.getId());
        assertEquals(5, updatedReview.getRating());
        assertEquals("更新后的评价", updatedReview.getComment());
    }

    @Test
    void testDeleteReview() {
        // 提交评价
        CooperationReview review = new CooperationReview();
        review.setOrderId(orderId);
        review.setReviewFrom(String.valueOf(purchaserId));
        review.setReviewTo(String.valueOf(farmerId));
        review.setTargetId(farmerInfoId);
        review.setRating(4);
        review.setComment("待删除的评价");
        cooperationReviewService.submitReview(review);

        // 删除评价
        cooperationReviewService.deleteReview(review.getId());

        // 验证评价已删除
        CooperationReview deletedReview = cooperationReviewService.getById(review.getId());
        assertNull(deletedReview);
    }

    @Test
    void testReviewFromBothSides() {
        // 采购方评价农民
        CooperationReview purchaserReview = new CooperationReview();
        purchaserReview.setOrderId(orderId);
        purchaserReview.setReviewFrom(String.valueOf(purchaserId));
        purchaserReview.setReviewTo(String.valueOf(farmerId));
        purchaserReview.setTargetId(farmerInfoId);
        purchaserReview.setRating(5);
        purchaserReview.setComment("农民的产品质量很好");
        cooperationReviewService.submitReview(purchaserReview);

        // 农民评价采购方
        CooperationReview farmerReview = new CooperationReview();
        farmerReview.setOrderId(orderId);
        farmerReview.setReviewFrom(String.valueOf(farmerId));
        farmerReview.setReviewTo(String.valueOf(purchaserId));
        farmerReview.setTargetId(purchaserInfoId);
        farmerReview.setRating(4);
        farmerReview.setComment("采购方付款及时，合作愉快");
        cooperationReviewService.submitReview(farmerReview);

        // 验证双方评价都存在
        CooperationReview savedPurchaserReview = cooperationReviewService.getById(purchaserReview.getId());
        CooperationReview savedFarmerReview = cooperationReviewService.getById(farmerReview.getId());

        assertNotNull(savedPurchaserReview);
        assertNotNull(savedFarmerReview);
        assertEquals(5, savedPurchaserReview.getRating());
        assertEquals(4, savedFarmerReview.getRating());
    }
}