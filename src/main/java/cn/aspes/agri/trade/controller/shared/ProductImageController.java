package cn.aspes.agri.trade.controller.shared;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.entity.ProductImage;
import cn.aspes.agri.trade.enums.ProductImageType;
import cn.aspes.agri.trade.service.ProductImageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 共享 - 产品图片管理控制器
 */
@Tag(name = "共享 - 产品图片")
@RestController
@RequestMapping("/api/shared/product-images")
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
