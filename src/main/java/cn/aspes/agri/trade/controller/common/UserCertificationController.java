package cn.aspes.agri.trade.controller.common;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.dto.UserCertificationRequest;
import cn.aspes.agri.trade.entity.UserCertificationApply;
import cn.aspes.agri.trade.security.CustomUserDetails;
import cn.aspes.agri.trade.service.UserCertificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 通用 - 用户认证申请控制器
 */
@Tag(name = "通用 - 用户认证")
@RestController
@RequestMapping("/api/common/certifications")
public class UserCertificationController {
    
    @Resource
    private UserCertificationService certificationService;
    
    @Operation(summary = "提交认证申请")
    @PostMapping("/apply")
    public Result<Long> submitCertification(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @Valid @RequestBody UserCertificationRequest request) {
        Long applyId = certificationService.submitCertification(userDetails.getId(), request);
        return Result.success(applyId);
    }
    
    @Operation(summary = "获取用户认证申请状态")
    @GetMapping("/status/{applyType}")
    public Result<UserCertificationApply> getCertificationStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                   @PathVariable String applyType) {
        UserCertificationApply apply = certificationService.getUserCertification(userDetails.getId(), applyType);
        return Result.success(apply);
    }
}
