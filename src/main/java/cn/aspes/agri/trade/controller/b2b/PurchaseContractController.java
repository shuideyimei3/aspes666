package cn.aspes.agri.trade.controller.b2b;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.converter.EntityVOConverter;
import cn.aspes.agri.trade.dto.ContractRequest;
import cn.aspes.agri.trade.entity.PurchaseContract;
import cn.aspes.agri.trade.enums.UserRole;
import cn.aspes.agri.trade.security.CustomUserDetails;
import cn.aspes.agri.trade.service.PurchaseContractService;
import cn.aspes.agri.trade.util.ImageProcessingUtil;
import cn.aspes.agri.trade.vo.PurchaseContractVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * B端 - 采购合同管理控制器
 */
@Tag(name = "B端 - 采购合同")
@RestController
@RequestMapping("/api/b2b/contracts")
public class PurchaseContractController {
    
    @Resource
    private PurchaseContractService purchaseContractService;
    
    @Resource
    private EntityVOConverter entityVOConverter;
    
    @Operation(summary = "创建合同")
    @PostMapping(consumes = {"multipart/form-data"})
    public Result<Long> createContract(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @RequestPart(value = "signFile", required = false) MultipartFile signFile,
                                       @Valid @ModelAttribute ContractRequest request) {
        // 将文件设置到请求对象中
        request.setSignFile(signFile);
        Long contractId = purchaseContractService.createContract(userDetails.getId(), request);
        return Result.success(contractId);
    }
    
    @Operation(summary = "签署合同")
    @PutMapping(value = "/{contractId}/sign", consumes = {"multipart/form-data"})
    public Result<Void> signContract(@PathVariable Long contractId,
                                      @AuthenticationPrincipal CustomUserDetails userDetails,
                                      @RequestPart(value = "signFile", required = true) MultipartFile signFile) {
        // 检查签字文件大小
        ImageProcessingUtil.checkFileSize(signFile);
        
        String role = userDetails.getRole() == UserRole.FARMER ? "farmer" : "purchaser";
        purchaseContractService.signContract(contractId, userDetails.getId(), signFile, role);
        return Result.success();
    }
    
    @Operation(summary = "撤回合同")
    @PutMapping("/{contractId}/withdraw")
    public Result<Void> withdrawContract(@PathVariable Long contractId,
                                        @AuthenticationPrincipal CustomUserDetails userDetails,
                                        @RequestParam(required = false) String reason) {
        purchaseContractService.withdrawContract(contractId, userDetails.getId(), reason);
        return Result.success();
    }
    
    @Operation(summary = "拒签合同")
    @PutMapping("/{contractId}/reject")
    public Result<Void> rejectContract(@PathVariable Long contractId,
                                      @AuthenticationPrincipal CustomUserDetails userDetails,
                                      @RequestParam(required = false) String reason) {
        purchaseContractService.rejectContract(contractId, userDetails.getId(), reason);
        return Result.success();
    }
    
    @Operation(summary = "终止合同")
    @PutMapping("/{contractId}/terminate")
    public Result<Void> terminateContract(@PathVariable Long contractId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails,
                                         @RequestParam(required = false) String reason) {
        purchaseContractService.terminateContract(contractId, userDetails.getId(), reason);
        return Result.success();
    }
    
    @Operation(summary = "查询我的合同列表")
    @GetMapping("/my")
    public Result<IPage<PurchaseContractVO>> listMyContracts(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                            @RequestParam(defaultValue = "1") int pageNum,
                                                            @RequestParam(defaultValue = "10") int pageSize) {
        String role = userDetails.getRole() == UserRole.FARMER ? "farmer" : "purchaser";
        IPage<PurchaseContract> page = purchaseContractService.listMyContracts(userDetails.getId(), role, pageNum, pageSize);
        IPage<PurchaseContractVO> voPage = entityVOConverter.toPurchaseContractVOPage(page);
        return Result.success(voPage);
    }
    
    @Operation(summary = "查询合同详情")
    @GetMapping("/{contractId}")
    public Result<PurchaseContractVO> getContract(@PathVariable Long contractId) {
        PurchaseContract contract = purchaseContractService.getById(contractId);
        PurchaseContractVO vo = entityVOConverter.toPurchaseContractVO(contract);
        return Result.success(vo);
    }
}