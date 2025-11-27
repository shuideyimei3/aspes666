package cn.aspes.agri.trade.controller.b2c;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.dto.FarmerInfoRequest;
import cn.aspes.agri.trade.entity.FarmerInfo;
import cn.aspes.agri.trade.enums.UserRole;
import cn.aspes.agri.trade.exception.BusinessException;
import cn.aspes.agri.trade.security.CustomUserDetails;
import cn.aspes.agri.trade.service.FarmerInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * C端 - 农户信息管理控制器
 */
@Tag(name = "C端 - 农户管理")
@RestController
@RequestMapping("/api/c2c/farmer-info")
public class FarmerInfoController {
    
    @Resource
    private FarmerInfoService farmerInfoService;
    
    @Operation(summary = "获取农户详情")
    @GetMapping("/{id}")
    public Result<FarmerInfo> getDetail(@PathVariable Long id) {
        FarmerInfo farmerInfo = farmerInfoService.getById(id);
        return Result.success(farmerInfo);
    }
    
    @Operation(summary = "获取当前用户的农户信息")
    @GetMapping("/my")
    @PreAuthorize("hasRole('FARMER')")
    public Result<FarmerInfo> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        FarmerInfo farmerInfo = farmerInfoService.getByUserId(userDetails.getId());
        return Result.success(farmerInfo);
    }
    
    @Operation(summary = "提交农户信息")
    @PostMapping
    @PreAuthorize("hasRole('FARMER')")
    public Result<Void> submitInfo(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @RequestPart(value = "idCardFrontFile", required = true) MultipartFile idCardFrontFile,
                                     @RequestPart(value = "idCardBackFile", required = true) MultipartFile idCardBackFile,
                                     @Valid @ModelAttribute FarmerInfoRequest request) {
        request.setIdCardFrontFile(idCardFrontFile);
        request.setIdCardBackFile(idCardBackFile);
        farmerInfoService.submitFarmerInfo(userDetails.getId(), request);
        return Result.success();
    }
    
    @Operation(summary = "修改农户信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FARMER')")
    public Result<Void> updateInfo(@PathVariable Long id,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    @Valid @RequestBody FarmerInfoRequest request) {
        farmerInfoService.updateFarmerInfo(id, userDetails.getId(), request);
        return Result.success();
    }
}