package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.entity.CooperationReview;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 合作评价服务接口
 */
public interface CooperationReviewService extends IService<CooperationReview> {
    
    /**
     * 提交评价
     */
    void submitReview(CooperationReview review, Long currentUserId);
    
    /**
     * 查询我的评价
     */
    Page<CooperationReview> listMyReviews(Long userId, Integer current, Integer size);
    
    /**
     * 查询获得的评价
     */
    Page<CooperationReview> listReceivedReviews(Long targetId, Integer current, Integer size);
    
    /**
     * 修改评价
     */
    void updateReview(Long reviewId, Integer rating, String comment, Long currentUserId);
    
    /**
     * 删除评价
     */
    void deleteReview(Long reviewId, Long currentUserId);
}