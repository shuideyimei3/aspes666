package cn.aspes.agri.trade.controller.common;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.entity.OriginArea;
import cn.aspes.agri.trade.service.OriginAreaService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 共享 - 产地管理控制器
 */
@Tag(name = "共享 - 产地管理")
@RestController
@RequestMapping("/api/shared/origin-area")
@RequiredArgsConstructor
public class OriginAreaController {
    
    private final OriginAreaService originAreaService;
    
    @Operation(summary = "分页查询产地")
    @GetMapping("/page")
    public Result<Page<OriginArea>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Boolean isPovertyArea) {
        return Result.success(originAreaService.pageQuery(current, size, province, city, isPovertyArea));
    }
    
    @Operation(summary = "获取产地详情")
    @GetMapping("/{id}")
    public Result<OriginArea> getById(@PathVariable Integer id) {
        return Result.success(originAreaService.getById(id));
    }
    
    @Operation(summary = "新增产地")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> save(@Valid @RequestBody OriginArea originArea) {
        originAreaService.save(originArea);
        return Result.success();
    }
    
    @Operation(summary = "更新产地")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@Valid @RequestBody OriginArea originArea) {
        originAreaService.updateById(originArea);
        return Result.success();
    }
    
    @Operation(summary = "删除产地")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Integer id) {
        originAreaService.removeById(id);
        return Result.success();
    }
}
