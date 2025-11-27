package cn.aspes.agri.trade.controller.b2c;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.converter.EntityVOConverter;
import cn.aspes.agri.trade.dto.FarmerProductRequest;
import cn.aspes.agri.trade.dto.ProductImageRequest;
import cn.aspes.agri.trade.entity.FarmerProduct;
import cn.aspes.agri.trade.security.CustomUserDetails;
import cn.aspes.agri.trade.service.FarmerInfoService;
import cn.aspes.agri.trade.service.FarmerProductService;
import cn.aspes.agri.trade.util.ImageProcessingUtil;
import cn.aspes.agri.trade.vo.FarmerProductVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * C端 - 产品管理控制器
 */
@Tag(name = "C端 - 产品管理")
@RestController
@RequestMapping("/api/c2c/products")
public class ProductController {
    
    @Resource
    private FarmerProductService farmerProductService;
    
    @Resource
    private FarmerInfoService farmerInfoService;
    
    @Resource
    private EntityVOConverter entityVOConverter;
    
    @Operation(summary = "发布产品")
    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('FARMER')")
    public Result<Long> publishProduct(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @Valid @ModelAttribute FarmerProductRequest request) {
        // 检查产品图片大小
        if (request.getProductImageDetails() != null) {
            for (ProductImageRequest imageRequest : request.getProductImageDetails()) {
                try {
                    ImageProcessingUtil.checkFileSize(imageRequest.getFile());
                } catch (IllegalArgumentException e) {
                    return Result.error(400, e.getMessage());
                }
            }
        }
        
        Long farmerId = farmerInfoService.getByUserId(userDetails.getId()).getId();
        Long productId = farmerProductService.publishProduct(farmerId, request);
        return Result.success(productId);
    }
    
    @Operation(summary = "更新产品")
    @PutMapping(value = "/{productId}", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('FARMER')")
    public Result<Void> updateProduct(@PathVariable Long productId,
                                       @AuthenticationPrincipal CustomUserDetails userDetails,
                                       @Valid @ModelAttribute FarmerProductRequest request) {
        // 检查产品图片大小
        if (request.getProductImageDetails() != null) {
            for (ProductImageRequest imageRequest : request.getProductImageDetails()) {
                ImageProcessingUtil.checkFileSize(imageRequest.getFile());
            }
        }
        
        Long farmerId = farmerInfoService.getByUserId(userDetails.getId()).getId();
        farmerProductService.updateProduct(productId, farmerId, request);
        return Result.success();
    }
    
    @Operation(summary = "产品上架")
    @PutMapping("/{productId}/on-sale")
    @PreAuthorize("hasRole('FARMER')")
    public Result<Void> onSaleProduct(@PathVariable Long productId,
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long farmerId = farmerInfoService.getByUserId(userDetails.getId()).getId();
        farmerProductService.onSale(productId, farmerId);
        return Result.success();
    }
    
    @Operation(summary = "产品下架")
    @PutMapping("/{productId}/off-sale")
    @PreAuthorize("hasRole('FARMER')")
    public Result<Void> offSaleProduct(@PathVariable Long productId,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long farmerId = farmerInfoService.getByUserId(userDetails.getId()).getId();
        farmerProductService.offSale(productId, farmerId);
        return Result.success();
    }
    
    @Operation(summary = "查询产品列表")
    @GetMapping
    public Result<IPage<FarmerProductVO>> listProducts(@RequestParam(defaultValue = "1") int pageNum,
                                                      @RequestParam(defaultValue = "10") int pageSize,
                                                      @RequestParam(required = false) Long categoryId,
                                                      @RequestParam(required = false) Integer originAreaId,
                                                      @RequestParam(required = false) String status) {
        IPage<FarmerProductVO> voPage = farmerProductService.listProductsWithImages(pageNum, pageSize, categoryId, originAreaId, status);
        return Result.success(voPage);
    }
    
    @Operation(summary = "查询我的产品列表")
    @GetMapping("/my")
    @PreAuthorize("hasRole('FARMER')")
    public Result<IPage<FarmerProductVO>> listMyProducts(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                        @RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        Long farmerId = farmerInfoService.getByUserId(userDetails.getId()).getId();
        IPage<FarmerProductVO> voPage = farmerProductService.listMyProductsWithImages(farmerId, pageNum, pageSize);
        return Result.success(voPage);
    }
    
    @Operation(summary = "查询产品详情")
    @GetMapping("/{productId}")
    public Result<FarmerProductVO> getProduct(@PathVariable Long productId) {
        FarmerProductVO vo = farmerProductService.getProductWithImagesById(productId);
        return Result.success(vo);
    }
}