package cn.aspes.agri.trade.service.impl;

import cn.aspes.agri.trade.dto.FarmerProductRequest;
import cn.aspes.agri.trade.entity.FarmerProduct;
import cn.aspes.agri.trade.entity.StockReservation;
import cn.aspes.agri.trade.enums.ProductStatus;
import cn.aspes.agri.trade.exception.BusinessException;
import cn.aspes.agri.trade.mapper.FarmerProductMapper;
import cn.aspes.agri.trade.mapper.StockReservationMapper;
import cn.aspes.agri.trade.service.FarmerProductService;
import cn.aspes.agri.trade.service.FileUploadService;
import cn.aspes.agri.trade.service.ProductImageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 农户产品服务实现类
 */
@Service
public class FarmerProductServiceImpl extends ServiceImpl<FarmerProductMapper, FarmerProduct> implements FarmerProductService {
    
    @Resource
    private StockReservationMapper stockReservationMapper;
    
    @Resource
    private FileUploadService fileUploadService;
    
    @Resource
    private ProductImageService productImageService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishProduct(Long farmerId, FarmerProductRequest request) {
        // 参数验证
        if (request.getProductImages() == null || request.getProductImages().isEmpty()) {
            throw new BusinessException("产品发布时至少需要上传一张图片");
        }
        
        // 创建产品
        FarmerProduct product = new FarmerProduct();
        BeanUtils.copyProperties(request, product);
        product.setFarmerId(farmerId);
        product.setStatus(ProductStatus.ON_SALE);
        
        save(product);
        
        // 处理产品图片
        handleProductImages(product.getId(), request.getProductImages());
        
        return product.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(Long productId, Long farmerId, FarmerProductRequest request) {
        FarmerProduct product = getById(productId);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        
        if (!product.getFarmerId().equals(farmerId)) {
            throw new BusinessException("无权修改该产品");
        }
        
        // 校验新库存
        Integer newStock = request.getStock();
        if (newStock != null && newStock != product.getStock()) {
            Integer reservedQuantity = stockReservationMapper.selectCount(
                    new LambdaQueryWrapper<StockReservation>()
                            .eq(StockReservation::getProductId, productId)
                            .eq(StockReservation::getStatus, "reserved")
            ).intValue();
            
            if (newStock < reservedQuantity) {
                throw new BusinessException(
                    String.format("产品库存修改失败：新库存(%d)不能小于已预留数量(%d)", newStock, reservedQuantity)
                );
            }
        }
        
        BeanUtils.copyProperties(request, product);
        updateById(product);
        
        // 处理产品图片（如果提供了新图片，则替换旧图片）
        if (request.getProductImages() != null && !request.getProductImages().isEmpty()) {
            // 先删除旧图片
            productImageService.deleteByProductId(productId);
            // 上传新图片
            handleProductImages(productId, request.getProductImages());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onSale(Long productId, Long farmerId) {
        FarmerProduct product = getById(productId);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        
        if (!product.getFarmerId().equals(farmerId)) {
            throw new BusinessException("无权操作该产品");
        }
        
        product.setStatus(ProductStatus.ON_SALE);
        updateById(product);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offSale(Long productId, Long farmerId) {
        FarmerProduct product = getById(productId);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        
        if (!product.getFarmerId().equals(farmerId)) {
            throw new BusinessException("无权操作该产品");
        }
        
        // ✅ 新增：下架前检查是否存在未完成订单的库存预留
        Integer activeReservations = stockReservationMapper.selectCount(
                new LambdaQueryWrapper<StockReservation>()
                        .eq(StockReservation::getProductId, productId)
                        .eq(StockReservation::getStatus, "reserved")
        ).intValue();
        
        if (activeReservations > 0) {
            throw new BusinessException(
                String.format("产品下架失败：存在 %d 个未完成的订单预留库存，请先完成或取消这些订单", activeReservations)
            );
        }
        
        product.setStatus(ProductStatus.OFF_SALE);
        updateById(product);
    }
    
    @Override
    public IPage<FarmerProduct> listProducts(int pageNum, int pageSize, Long categoryId, Integer originAreaId, String status) {
        Page<FarmerProduct> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FarmerProduct> wrapper = new LambdaQueryWrapper<>();
        
        if (categoryId != null) {
            wrapper.eq(FarmerProduct::getCategoryId, categoryId);
        }
        if (originAreaId != null) {
            wrapper.eq(FarmerProduct::getOriginAreaId, originAreaId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(FarmerProduct::getStatus, ProductStatus.valueOf(status.toUpperCase()));
        } else {
            wrapper.eq(FarmerProduct::getStatus, ProductStatus.ON_SALE);
        }
        
        wrapper.orderByDesc(FarmerProduct::getCreateTime);
        
        return page(page, wrapper);
    }
    
    @Override
    public IPage<FarmerProduct> listMyProducts(Long farmerId, int pageNum, int pageSize) {
        Page<FarmerProduct> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FarmerProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FarmerProduct::getFarmerId, farmerId);
        wrapper.orderByDesc(FarmerProduct::getCreateTime);
        
        return page(page, wrapper);
    }
    
    /**
     * 处理产品图片上传
     */
    private void handleProductImages(Long productId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String imageUrl = fileUploadService.uploadProductImage(file);
                imageUrls.add(imageUrl);
            }
        }
        
        // 保存上传的图片信息到数据库
        if (!imageUrls.isEmpty()) {
            productImageService.saveProductImages(productId, imageUrls, "COVER");
        }
    }
}
