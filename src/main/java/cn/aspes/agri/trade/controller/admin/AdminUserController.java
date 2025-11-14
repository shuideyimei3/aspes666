package cn.aspes.agri.trade.controller.admin;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.dto.AuditRequest;
import cn.aspes.agri.trade.entity.FarmerInfo;
import cn.aspes.agri.trade.entity.PurchaserInfo;
import cn.aspes.agri.trade.entity.User;
import cn.aspes.agri.trade.service.FarmerInfoService;
import cn.aspes.agri.trade.service.PurchaserInfoService;
import cn.aspes.agri.trade.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理 - 用户管理控制器
 */
@Tag(name = "后台管理 - 用户管理")
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    
    @Resource
    private UserService userService;
    
    @Resource
    private FarmerInfoService farmerInfoService;
    
    @Resource
    private PurchaserInfoService purchaserInfoService;
    
    @Operation(summary = "分页查询用户列表")
    @GetMapping("/page")
    public Result<Page<User>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer isCertified) {
        return Result.success(userService.pageUsers(current, size, role, isCertified));
    }
    
    @Operation(summary = "禁用/启用用户")
    @PutMapping("/{userId}/status")
    public Result<Void> toggleStatus(@PathVariable Long userId, @RequestParam Integer isDelete) {
        userService.toggleUserStatus(userId, isDelete);
        return Result.success();
    }
    
    @Operation(summary = "分页查询农户信息（审核）")
    @GetMapping("/farmers/page")
    public Result<Page<FarmerInfo>> pageFarmers(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String auditStatus) {
        return Result.success(farmerInfoService.pageFarmers(current, size, auditStatus));
    }
    
    @Operation(summary = "审核农户信息")
    @PutMapping("/farmers/{id}/audit")
    public Result<Void> auditFarmerInfo(@PathVariable Long id, @Valid @RequestBody AuditRequest request) {
        farmerInfoService.auditFarmerInfo(id, request);
        return Result.success();
    }
    
    @Operation(summary = "分页查询采购方信息（审核）")
    @GetMapping("/purchasers/page")
    public Result<Page<PurchaserInfo>> pagePurchasers(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String auditStatus) {
        return Result.success(purchaserInfoService.pagePurchasers(current, size, auditStatus));
    }
    
    @Operation(summary = "审核采购方信息")
    @PutMapping("/purchasers/{id}/audit")
    public Result<Void> auditPurchaserInfo(@PathVariable Long id, @Valid @RequestBody AuditRequest request) {
        purchaserInfoService.auditPurchaserInfo(id, request);
        return Result.success();
    }
}
