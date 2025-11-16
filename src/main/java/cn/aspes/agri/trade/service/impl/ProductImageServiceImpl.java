package cn.aspes.agri.trade.service.impl;

import cn.aspes.agri.trade.dto.ProductImageRequest;
import cn.aspes.agri.trade.entity.ProductImage;
import cn.aspes.agri.trade.enums.ProductImageType;
import cn.aspes.agri.trade.mapper.ProductImageMapper;
import cn.aspes.agri.trade.service.FileUploadService;
import cn.aspes.agri.trade.service.ProductImageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 产品图片服务实现
 */
@Slf4j
@Service
public class ProductImageServiceImpl extends ServiceImpl<ProductImageMapper, ProductImage> implements ProductImageService {
    
    @Resource
    private FileUploadService fileUploadService;
    
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
    @Transactional(rollbackFor = Exception.class)
    public void saveProductImages(Long productId, List<ProductImageRequest> imageRequests) {
        if (imageRequests == null || imageRequests.isEmpty()) {
            return;
        }
        
        List<ProductImage> images = new ArrayList<>();
        List<String> uploadedUrls = new ArrayList<>();
        
        try {
            for (int i = 0; i < imageRequests.size(); i++) {
                ProductImageRequest request = imageRequests.get(i);
                MultipartFile file = request.getFile();
                
                if (file != null && !file.isEmpty()) {
                    String imageUrl = fileUploadService.uploadProductImage(file);
                    uploadedUrls.add(imageUrl);  // 记录已上传的图片URL
                    
                    ProductImage image = new ProductImage();
                    image.setProductId(productId);
                    image.setImageUrl(imageUrl);
                    image.setImageType(request.getImageType());
                    
                    // 如果没有指定排序，则使用索引作为默认排序
                    Integer sort = request.getSort();
                    if (sort == null) {
                        sort = i;
                    }
                    image.setSort(sort);
                    
                    images.add(image);
                }
            }
            
            // 只有在数据库保存成功之前，才注册事务回滚回调删除上传的文件
            if (!uploadedUrls.isEmpty() && TransactionSynchronizationManager.isActualTransactionActive()) {
                registerImagesDeleteOnRollback(uploadedUrls);
            }
            
            if (!images.isEmpty()) {
                saveBatch(images);
            }
        } catch (Exception e) {
            // 如果上传或保存过程中发生异常，立即删除已上传的文件
            for (String url : uploadedUrls) {
                try {
                    fileUploadService.deleteFile(url);
                } catch (Exception deleteError) {
                    log.error("删除已上传的产品图片失败");
                }
            }
            throw e;  // 重新抛出异常
        }
    }
    
    /**
     * 注册事务回滚时的产品图片删除回调
     */
    private void registerImagesDeleteOnRollback(List<String> imageUrls) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                // status = TransactionSynchronization.STATUS_ROLLED_BACK 表示事务已回滚
                if (status == STATUS_ROLLED_BACK) {
                    for (String imageUrl : imageUrls) {
                        try {
                            fileUploadService.deleteFile(imageUrl);
                            log.info("事务回滚：已自动删除产品图片，URL={}", imageUrl);
                        } catch (Exception e) {
                            log.error("事务回滚时删除产品图片失败，URL={}", imageUrl, e);
                        }
                    }
                }
            }
        });
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
        // 先获取所有图片信息，以便删除 OSS 文件
        List<ProductImage> images = list(new LambdaQueryWrapper<ProductImage>()
                .eq(ProductImage::getProductId, productId));
        
        // 删除 OSS 上的文件
        for (ProductImage image : images) {
            try {
                if (image.getImageUrl() != null && !image.getImageUrl().isEmpty()) {
                    fileUploadService.deleteFile(image.getImageUrl());
                }
            } catch (Exception e) {
                log.warn("删除产品图片文件失败，URL={}", image.getImageUrl(), e);
            }
        }
        
        // 再从数据库删除记录
        remove(new LambdaQueryWrapper<ProductImage>()
                .eq(ProductImage::getProductId, productId));
    }
}