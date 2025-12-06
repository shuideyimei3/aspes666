package cn.aspes.agri.trade.controller.admin;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.entity.FarmerProduct;
import cn.aspes.agri.trade.enums.ProductStatus;
import cn.aspes.agri.trade.service.FarmerProductService;
import cn.aspes.agri.trade.vo.FarmerProductVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 后台管理 - 产品管理控制器
 * 
 * 提供管理员对平台产品的管理功能，包括产品查询、状态管理等
 */
@Tag(name = "后台管理 - 产品管理")
@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {
    
    @Resource
    private FarmerProductService farmerProductService;
    
    /**
     * 分页查询所有产品
     * 
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @param categoryId 产品分类ID
     * @param originAreaId 产地ID
     * @param status 产品状态
     * @return 产品列表分页结果
     */
    @Operation(summary = "分页查询所有产品")
    @GetMapping("/page")
    public Result<IPage<FarmerProductVO>> pageProducts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer originAreaId,
            @RequestParam(required = false) String status) {
        IPage<FarmerProductVO> result = farmerProductService.listProductsWithImages(pageNum, pageSize, categoryId, originAreaId, status);
        return Result.success(result);
    }
    
    /**
     * 根据产品ID查询产品详情
     * 
     * @param productId 产品ID
     * @return 产品详情
     */
    @Operation(summary = "查询产品详情")
    @GetMapping("/{productId}")
    public Result<FarmerProductVO> getProductDetail(@PathVariable Long productId) {
        FarmerProductVO product = farmerProductService.getProductWithImagesById(productId);
        return Result.success(product);
    }
    
    /**
     * 强制产品下架
     * 
     * @param productId 产品ID
     * @param reason 下架原因
     * @return 操作结果
     */
    @Operation(summary = "强制产品下架")
    @PutMapping("/{productId}/force-off-sale")
    public Result<Void> forceOffSale(@PathVariable Long productId, @RequestParam(required = false) String reason) {
        FarmerProduct product = farmerProductService.getProductById(productId);
        // 管理员强制下架，不需要验证权限
        farmerProductService.offSale(productId, product.getFarmerId());
        return Result.success();
    }
    
    /**
     * 强制产品上架
     * 
     * @param productId 产品ID
     * @return 操作结果
     */
    @Operation(summary = "强制产品上架")
    @PutMapping("/{productId}/force-on-sale")
    public Result<Void> forceOnSale(@PathVariable Long productId) {
        FarmerProduct product = farmerProductService.getProductById(productId);
        // 管理员强制上架，不需要验证权限
        farmerProductService.onSale(productId, product.getFarmerId());
        return Result.success();
    }
    
    /**
     * 删除产品
     * 
     * @param productId 产品ID
     * @return 操作结果
     */
    @Operation(summary = "删除产品")
    @DeleteMapping("/{productId}")
    public Result<Void> deleteProduct(@PathVariable Long productId) {
        FarmerProduct product = farmerProductService.getProductById(productId);
        // 管理员删除产品
        farmerProductService.removeById(productId);
        return Result.success();
    }
    
    /**
     * 根据产品名称搜索产品
     * 
     * @param keyword 关键词
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @return 搜索结果
     */
    @Operation(summary = "根据产品名称搜索产品")
    @GetMapping("/search")
    public Result<IPage<FarmerProduct>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<FarmerProduct> result = farmerProductService.searchProductsByName(keyword, pageNum, pageSize);
        return Result.success(result);
    }
    
    /**
     * 获取产品统计信息
     * 
     * @return 统计信息
     */
    @Operation(summary = "获取产品统计信息")
    @GetMapping("/statistics")
    public Result<Object> getProductStatistics() {
        // 获取总产品数
        long totalCount = farmerProductService.count();
        
        // 获取各状态产品数
        long onlineCount = farmerProductService.count(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.aspes.agri.trade.entity.FarmerProduct>()
                .eq(cn.aspes.agri.trade.entity.FarmerProduct::getStatus, cn.aspes.agri.trade.enums.ProductStatus.ON_SALE));
                
        long offlineCount = farmerProductService.count(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.aspes.agri.trade.entity.FarmerProduct>()
                .eq(cn.aspes.agri.trade.entity.FarmerProduct::getStatus, cn.aspes.agri.trade.enums.ProductStatus.OFF_SALE));
        
        // 构建统计结果
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", totalCount);
        statistics.put("onlineCount", onlineCount);
        statistics.put("offlineCount", offlineCount);
        
        return Result.success(statistics);
    }
}