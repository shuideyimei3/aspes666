package cn.aspes.agri.trade.controller.b2b;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.dto.PurchaserInfoRequest;
import cn.aspes.agri.trade.entity.PurchaserInfo;
import cn.aspes.agri.trade.exception.BusinessException;
import cn.aspes.agri.trade.security.CustomUserDetails;
import cn.aspes.agri.trade.service.PurchaserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * B端 - 采购方信息管理控制器
 */
@Tag(name = "B端 - 采购方管理")
@RestController
@RequestMapping("/api/b2b/purchaser-info")
public class PurchaserInfoController {
    
    @Resource
    private PurchaserInfoService purchaserInfoService;
    
    @Operation(summary = "提交采购方信息")
    @PostMapping
    public Result<Void> submitPurchaserInfo(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @Valid @RequestBody PurchaserInfoRequest request) {
        purchaserInfoService.submitPurchaserInfo(userDetails.getId(), request);
        return Result.success();
    }
    
    @Operation(summary = "获取采购方详情")
    @GetMapping("/{id}")
    public Result<PurchaserInfo> getDetail(@PathVariable Long id) {
        PurchaserInfo purchaserInfo = purchaserInfoService.getById(id);
        if (purchaserInfo == null) {
            throw new BusinessException("采购方信息不存在");
        }
        return Result.success(purchaserInfo);
    }
    
    @Operation(summary = "获取当前用户的采购方信息")
    @GetMapping("/my")
    @PreAuthorize("hasRole('PURCHASER')")
    public Result<PurchaserInfo> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        PurchaserInfo purchaserInfo = purchaserInfoService.getByUserId(userDetails.getId());
        return Result.success(purchaserInfo);
    }
    
    @Operation(summary = "修改采购方信息")
    @PutMapping("/{id}")
    public Result<Void> updatePurchaserInfo(@PathVariable Long id,
                                           @AuthenticationPrincipal CustomUserDetails userDetails,
                                           @Valid @RequestBody PurchaserInfoRequest request) {
        purchaserInfoService.updatePurchaserInfo(id, userDetails.getId(), request);
        return Result.success();
    }
}
