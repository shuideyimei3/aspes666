package cn.aspes.agri.trade.controller.admin;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.entity.UserCertificationApply;
import cn.aspes.agri.trade.service.UserCertificationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理 - 用户认证审核控制器
 */
@Tag(name = "后台管理 - 认证审核")
@RestController
@RequestMapping("/api/admin/certifications")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCertificationController {
    
    @Resource
    private UserCertificationService certificationService;
    
    @Operation(summary = "分页查询待审核的认证申请")
    @GetMapping("/pending")
    public Result<Page<UserCertificationApply>> pagePendingApplications(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String applyType) {
        Page<UserCertificationApply> page = certificationService.pagePendingApplications(current, size, applyType);
        return Result.success(page);
    }
    
    @Operation(summary = "分页查询所有认证申请")
    @GetMapping("/page")
    public Result<Page<UserCertificationApply>> pageApplications(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        Page<UserCertificationApply> page = certificationService.pageApplications(current, size, status);
        return Result.success(page);
    }
    
    @Operation(summary = "批准认证申请")
    @PutMapping("/{applyId}/approve")
    public Result<Void> approveCertification(
            @PathVariable Long applyId,
            @RequestParam(required = false) String adminRemark) {
        certificationService.approveCertification(applyId, adminRemark);
        return Result.success();
    }
    
    @Operation(summary = "拒绝认证申请")
    @PutMapping("/{applyId}/reject")
    public Result<Void> rejectCertification(
            @PathVariable Long applyId,
            @RequestParam String rejectReason) {
        certificationService.rejectCertification(applyId, rejectReason);
        return Result.success();
    }
}
