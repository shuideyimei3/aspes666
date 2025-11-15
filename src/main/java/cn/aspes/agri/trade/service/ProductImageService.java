package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.dto.ProductImageRequest;
import cn.aspes.agri.trade.entity.ProductImage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 产品图片服务
 */
public interface ProductImageService extends IService<ProductImage> {
    
    /**
     * 批量保存产品图片
     */
    void saveProductImages(Long productId, List<String> imageUrls, String imageType);
    
    /**
     * 批量保存产品图片（支持指定类型和排序）
     */
    void saveProductImages(Long productId, List<ProductImageRequest> imageRequests);
    
    /**
     * 查询产品图片列表
     */
    List<ProductImage> listByProductId(Long productId);
    
    /**
     * 删除产品图片
     */
    void deleteByProductId(Long productId);
}