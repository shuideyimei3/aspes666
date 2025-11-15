package cn.aspes.agri.trade.controller.admin;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.dto.ProductImageRequest;
import cn.aspes.agri.trade.entity.ProductImage;
import cn.aspes.agri.trade.enums.ProductImageType;
import cn.aspes.agri.trade.service.ProductImageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 共享 - 产品图片管理控制器
 */
@Tag(name = "后台管理 - 产品图片")
@RestController
@RequestMapping("/api/shared/product-images")
@PreAuthorize("hasRole('ADMIN')")
public class ProductImageController {
    
    @Resource
    private ProductImageService productImageService;
    
    @Operation(summary = "按产品ID查询图片列表")
    @GetMapping("/product/{productId}")
    public Result<List<ProductImage>> listByProduct(@PathVariable Long productId) {
        QueryWrapper<ProductImage> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId).orderByAsc("sort");
        List<ProductImage> images = productImageService.list(wrapper);
        return Result.success(images);
    }
    
    @Operation(summary = "上传产品图片")
    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public Result<Long> uploadImage(@RequestParam Long productId,
                                    @RequestParam ProductImageType imageType,
                                    @RequestParam(required = false) Integer sort,
                                    @RequestParam("file") MultipartFile file) {
        ProductImageRequest request = new ProductImageRequest();
        request.setFile(file);
        request.setImageType(imageType);
        request.setSort(sort);
        
        // 创建一个临时的图片请求列表
        List<ProductImageRequest> requests = List.of(request);
        productImageService.saveProductImages(productId, requests);
        
        // 返回最新添加的图片ID
        QueryWrapper<ProductImage> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId).orderByDesc("create_time").last("LIMIT 1");
        ProductImage image = productImageService.getOne(wrapper);
        
        return Result.success(image != null ? image.getId() : null);
    }
    
    @Operation(summary = "批量上传产品图片")
    @PostMapping(value = "/batch-upload", consumes = {"multipart/form-data"})
    public Result<List<Long>> batchUploadImages(@RequestParam Long productId,
                                                @ModelAttribute @Valid ProductImageRequest[] requests) {
        // 将数组转换为列表
        List<ProductImageRequest> requestList = List.of(requests);
        productImageService.saveProductImages(productId, requestList);
        
        // 返回新添加的图片ID列表
        QueryWrapper<ProductImage> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId).orderByDesc("create_time").last("LIMIT " + requestList.size());
        List<ProductImage> images = productImageService.list(wrapper);
        
        List<Long> ids = images.stream().map(ProductImage::getId).toList();
        return Result.success(ids);
    }
    
    @Operation(summary = "添加产品图片")
    @PostMapping
    public Result<Long> addImage(@RequestBody ProductImage productImage) {
        productImageService.save(productImage);
        return Result.success(productImage.getId());
    }
    
    @Operation(summary = "修改产品图片信息")
    @PutMapping("/{id}")
    public Result<Void> updateImage(@PathVariable Long id, @RequestBody ProductImage productImage) {
        productImage.setId(id);
        productImageService.updateById(productImage);
        return Result.success();
    }
    
    @Operation(summary = "删除产品图片")
    @DeleteMapping("/{id}")
    public Result<Void> deleteImage(@PathVariable Long id) {
        productImageService.removeById(id);
        return Result.success();
    }
    
    @Operation(summary = "调整图片顺序")
    @PutMapping("/{id}/sort")
    public Result<Void> updateSort(@PathVariable Long id, @RequestParam Integer sort) {
        ProductImage image = productImageService.getById(id);
        if (image != null) {
            image.setSort(sort);
            productImageService.updateById(image);
        }
        return Result.success();
    }
    
    @Operation(summary = "分页查询产品图片")
    @GetMapping("/page")
    public Result<Page<ProductImage>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String imageType) {
        Page<ProductImage> page = new Page<>(current, size);
        QueryWrapper<ProductImage> wrapper = new QueryWrapper<>();
        
        if (productId != null) {
            wrapper.eq("product_id", productId);
        }
        if (imageType != null && !imageType.isEmpty()) {
            try {
                wrapper.eq("image_type", imageType.toUpperCase());
            } catch (Exception e) {
                // 无效的图片类型，忽略该过滤条件
            }
        }
        
        wrapper.orderByAsc("sort");
        return Result.success(productImageService.page(page, wrapper));
    }
}