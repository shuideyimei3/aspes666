package cn.aspes.agri.trade.controller.shared;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.entity.ProductCategory;
import cn.aspes.agri.trade.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 共享 - 产品分类管理控制器
 */
@Tag(name = "共享 - 产品分类")
@RestController
@RequestMapping("/api/shared/product-category")
@RequiredArgsConstructor
public class ProductCategoryController {
    
    private final ProductCategoryService productCategoryService;
    
    @Operation(summary = "获取树形分类列表")
    @GetMapping("/tree")
    public Result<List<ProductCategory>> getTree() {
        return Result.success(productCategoryService.getTreeList());
    }
    
    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    public Result<ProductCategory> getById(@PathVariable Long id) {
        return Result.success(productCategoryService.getById(id));
    }
    
    @Operation(summary = "新增分类")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> save(@Valid @RequestBody ProductCategory category) {
        productCategoryService.save(category);
        return Result.success();
    }
    
    @Operation(summary = "更新分类")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@Valid @RequestBody ProductCategory category) {
        productCategoryService.updateById(category);
        return Result.success();
    }
    
    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        productCategoryService.removeById(id);
        return Result.success();
    }
}
