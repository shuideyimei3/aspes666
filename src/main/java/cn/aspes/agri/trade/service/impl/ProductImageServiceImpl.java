package cn.aspes.agri.trade.service.impl;

import cn.aspes.agri.trade.entity.ProductImage;
import cn.aspes.agri.trade.enums.ProductImageType;
import cn.aspes.agri.trade.mapper.ProductImageMapper;
import cn.aspes.agri.trade.service.ProductImageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 产品图片服务实现
 */
@Service
public class ProductImageServiceImpl extends ServiceImpl<ProductImageMapper, ProductImage> implements ProductImageService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProductImages(Long productId, List<String> imageUrls, String imageType) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        
        List<ProductImage> images = new ArrayList<>();
        ProductImageType type = ProductImageType.valueOf(imageType.toUpperCase());
        
        for (int i = 0; i < imageUrls.size(); i++) {
            ProductImage image = new ProductImage();
            image.setProductId(productId);
            image.setImageUrl(imageUrls.get(i));
            image.setImageType(type);
            image.setSort(i);
            images.add(image);
        }
        
        saveBatch(images);
    }
    
    @Override
    public List<ProductImage> listByProductId(Long productId) {
        return list(new LambdaQueryWrapper<ProductImage>()
                .eq(ProductImage::getProductId, productId)
                .orderByAsc(ProductImage::getSort));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByProductId(Long productId) {
        remove(new LambdaQueryWrapper<ProductImage>()
                .eq(ProductImage::getProductId, productId));
    }
}
