package cn.aspes.agri.trade.service.impl;

import cn.aspes.agri.trade.entity.CooperationReview;
import cn.aspes.agri.trade.entity.FarmerInfo;
import cn.aspes.agri.trade.entity.PurchaseOrder;
import cn.aspes.agri.trade.entity.PurchaserInfo;
import cn.aspes.agri.trade.enums.OrderStatus;
import cn.aspes.agri.trade.exception.BusinessException;
import cn.aspes.agri.trade.mapper.CooperationReviewMapper;
import cn.aspes.agri.trade.mapper.PurchaseOrderMapper;
import cn.aspes.agri.trade.service.CooperationReviewService;
import cn.aspes.agri.trade.service.FarmerInfoService;
import cn.aspes.agri.trade.service.PurchaserInfoService;
import cn.aspes.agri.trade.util.SnowflakeIdGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 合作评价服务实现类
 */
@Service
@RequiredArgsConstructor
public class CooperationReviewServiceImpl extends ServiceImpl<CooperationReviewMapper, CooperationReview> implements CooperationReviewService {
    
    // ✅ 修复：移除PurchaseOrderService依赖，改用PurchaseOrderMapper防止循环依赖
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final FarmerInfoService farmerInfoService;
    private final PurchaserInfoService purchaserInfoService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitReview(CooperationReview review, Long currentUserId) {
        // ✅ 修复：使用Mapper直接查询，避免循环依赖
        // 验证订单状态
        PurchaseOrder order = purchaseOrderMapper.selectById(review.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException("只有已完成的订单才能评价");
        }
        
        // 验证评价者是否是订单的参与者
        FarmerInfo farmer = farmerInfoService.getByUserId(currentUserId);
        PurchaserInfo purchaser = purchaserInfoService.getByUserId(currentUserId);
        
        boolean isOrderParticipant = false;
        if (farmer != null && farmer.getId().equals(order.getFarmerId())) {
            isOrderParticipant = true;
            // 设置评价来源为农户
            review.setReviewFrom("FARMER_" + currentUserId);
            review.setReviewTo("PURCHASER_" + order.getPurchaserId());
            review.setTargetId(order.getPurchaserId());
        } else if (purchaser != null && purchaser.getId().equals(order.getPurchaserId())) {
            isOrderParticipant = true;
            // 设置评价来源为采购方
            review.setReviewFrom("PURCHASER_" + currentUserId);
            review.setReviewTo("FARMER_" + order.getFarmerId());
            review.setTargetId(order.getFarmerId());
        }
        
        if (!isOrderParticipant) {
            throw new BusinessException("只有订单参与者才能评价");
        }
        
        // 检查是否已评价
        long count = count(new LambdaQueryWrapper<CooperationReview>()
                .eq(CooperationReview::getOrderId, review.getOrderId())
                .eq(CooperationReview::getReviewFrom, review.getReviewFrom()));
        
        if (count > 0) {
            throw new BusinessException("已经评价过该订单");
        }
        
        // 验证评分范围
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new BusinessException("评分必须在1-5星之间");
        }
        
        // 验证角色互斥
        if (review.getReviewFrom().equals(review.getReviewTo())) {
            throw new BusinessException("评价方和被评价方不能相同");
        }
        
        review.setId(idGenerator.nextId());
        save(review);
    }
    
    @Override
    public Page<CooperationReview> listMyReviews(Long userId, Integer current, Integer size) {
        Page<CooperationReview> page = new Page<>(current, size);
        LambdaQueryWrapper<CooperationReview> wrapper = new LambdaQueryWrapper<>();
        
        // 查询userId发表的所有评价，可能是农户或采购方
        String farmerReviewFrom = "FARMER_" + userId;
        String purchaserReviewFrom = "PURCHASER_" + userId;
        
        wrapper.and(w -> w.eq(CooperationReview::getReviewFrom, farmerReviewFrom)
                         .or()
                         .eq(CooperationReview::getReviewFrom, purchaserReviewFrom));
        
        wrapper.orderByDesc(CooperationReview::getCreateTime);
        return page(page, wrapper);
    }
    
    @Override
    public Page<CooperationReview> listReceivedReviews(Long targetId, Integer current, Integer size) {
        Page<CooperationReview> page = new Page<>(current, size);
        LambdaQueryWrapper<CooperationReview> wrapper = new LambdaQueryWrapper<>();
        
        // 查询针对targetId的所有评价
        wrapper.eq(CooperationReview::getTargetId, targetId);
        wrapper.orderByDesc(CooperationReview::getCreateTime);
        
        return page(page, wrapper);
    }
    
    @Override
    public void updateReview(Long reviewId, Integer rating, String comment, Long currentUserId) {
        CooperationReview review = getById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        
        // 验证当前用户是否是评价的创建者
        // reviewFrom可能是"FARMER_" + userId或"PURCHASER_" + userId
        String farmerReviewFrom = "FARMER_" + currentUserId;
        String purchaserReviewFrom = "PURCHASER_" + currentUserId;
        
        if (!review.getReviewFrom().equals(farmerReviewFrom) && !review.getReviewFrom().equals(purchaserReviewFrom)) {
            throw new BusinessException("无权限修改此评价");
        }
        
        review.setRating(rating);
        review.setComment(comment);
        updateById(review);
    }
    
    @Override
    public void deleteReview(Long reviewId, Long currentUserId) {
        CooperationReview review = getById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        
        // 验证当前用户是否是评价的创建者
        // reviewFrom可能是"FARMER_" + userId或"PURCHASER_" + userId
        String farmerReviewFrom = "FARMER_" + currentUserId;
        String purchaserReviewFrom = "PURCHASER_" + currentUserId;
        
        if (!review.getReviewFrom().equals(farmerReviewFrom) && !review.getReviewFrom().equals(purchaserReviewFrom)) {
            throw new BusinessException("无权限删除此评价");
        }
        
        removeById(reviewId);
    }
}