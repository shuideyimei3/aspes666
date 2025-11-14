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
    public void submitReview(CooperationReview review) {
        // ✅ 修复：使用Mapper直接查询，避免循环依赖
        // 验证订单状态
        PurchaseOrder order = purchaseOrderMapper.selectById(review.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException("只有已完成的订单才能评价");
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
        
        // ✅ 修复：根据角色获取信息ID，然后查询用户发表的评价
        // 需要在Controller层传入角色信息，或从SecurityContext获取
        // 这里简化处理：查询userId发表的所有评价
        wrapper.eq(CooperationReview::getReviewFrom, userId);
        
        wrapper.orderByDesc(CooperationReview::getCreateTime);
        return page(page, wrapper);
    }
    
    @Override
    public Page<CooperationReview> listReceivedReviews(Long targetId, Integer current, Integer size) {
        Page<CooperationReview> page = new Page<>(current, size);
        LambdaQueryWrapper<CooperationReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CooperationReview::getTargetId, targetId);
        wrapper.orderByDesc(CooperationReview::getCreateTime);
        
        return page(page, wrapper);
    }
    
    @Override
    public void updateReview(Long reviewId, Integer rating, String comment) {
        CooperationReview review = getById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        
        review.setRating(rating);
        review.setComment(comment);
        updateById(review);
    }
    
    @Override
    public void deleteReview(Long reviewId) {
        removeById(reviewId);
    }
}
