package cn.aspes.agri.trade.controller.purchaser;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.converter.EntityVOConverter;
import cn.aspes.agri.trade.dto.PurchaseDemandRequest;
import cn.aspes.agri.trade.entity.PurchaseDemand;
import cn.aspes.agri.trade.security.CustomUserDetails;
import cn.aspes.agri.trade.service.PurchaseDemandService;
import cn.aspes.agri.trade.service.PurchaserInfoService;
import cn.aspes.agri.trade.vo.PurchaseDemandVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * B端 - 采购需求管理控制器
 */
@Tag(name = "B端 - 采购需求管理")
@RestController
@RequestMapping("/api/purchaser/demands")
public class PurchaseDemandController {
    
    @Resource
    private PurchaseDemandService purchaseDemandService;
    
    @Resource
    private PurchaserInfoService purchaserInfoService;
    
    @Resource
    private EntityVOConverter entityVOConverter;
    
    @Operation(summary = "发布采购需求")
    @PostMapping
    @PreAuthorize("hasRole('PURCHASER')")
    public Result<Long> publishDemand(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @Valid @RequestBody PurchaseDemandRequest request) {
        Long purchaserId = purchaserInfoService.getByUserId(userDetails.getId()).getId();
        Long demandId = purchaseDemandService.publishDemand(purchaserId, request);
        return Result.success(demandId);
    }
    
    @Operation(summary = "更新采购需求")
    @PutMapping("/{demandId}")
    @PreAuthorize("hasRole('PURCHASER')")
    public Result<Void> updateDemand(@PathVariable Long demandId,
                                      @AuthenticationPrincipal CustomUserDetails userDetails,
                                      @Valid @RequestBody PurchaseDemandRequest request) {
        Long purchaserId = purchaserInfoService.getByUserId(userDetails.getId()).getId();
        purchaseDemandService.updateDemand(demandId, purchaserId, request);
        return Result.success();
    }
    
    @Operation(summary = "关闭需求")
    @PutMapping("/{demandId}/close")
    @PreAuthorize("hasRole('PURCHASER')")
    public Result<Void> closeDemand(@PathVariable Long demandId,
                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long purchaserId = purchaserInfoService.getByUserId(userDetails.getId()).getId();
        purchaseDemandService.closeDemand(demandId, purchaserId);
        return Result.success();
    }
    
    @Operation(summary = "开启需求")
    @PutMapping("/{demandId}/open")
    @PreAuthorize("hasRole('PURCHASER')")
    public Result<Void> openDemand(@PathVariable Long demandId,
                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long purchaserId = purchaserInfoService.getByUserId(userDetails.getId()).getId();
        purchaseDemandService.openDemand(demandId, purchaserId);
        return Result.success();
    }
    
    @Operation(summary = "查询需求列表")
    @GetMapping
    public Result<IPage<PurchaseDemandVO>> listDemands(@RequestParam(defaultValue = "1") int pageNum,
                                                      @RequestParam(defaultValue = "10") int pageSize,
                                                      @RequestParam(required = false) Long categoryId,
                                                      @RequestParam(required = false) String status) {
        IPage<PurchaseDemand> page = purchaseDemandService.listDemands(pageNum, pageSize, categoryId, status);
        IPage<PurchaseDemandVO> voPage = entityVOConverter.toPurchaseDemandVOPage(page);
        return Result.success(voPage);
    }
    
    @Operation(summary = "查询我的需求列表")
    @GetMapping("/my")
    @PreAuthorize("hasRole('PURCHASER')")
    public Result<IPage<PurchaseDemandVO>> listMyDemands(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                        @RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        Long purchaserId = purchaserInfoService.getByUserId(userDetails.getId()).getId();
        IPage<PurchaseDemand> page = purchaseDemandService.listMyDemands(purchaserId, pageNum, pageSize);
        IPage<PurchaseDemandVO> voPage = entityVOConverter.toPurchaseDemandVOPage(page);
        return Result.success(voPage);
    }
    
    @Operation(summary = "查询需求详情")
    @GetMapping("/{demandId}")
    public Result<PurchaseDemandVO> getDemand(@PathVariable Long demandId) {
        PurchaseDemand demand = purchaseDemandService.getById(demandId);
        PurchaseDemandVO vo = entityVOConverter.toPurchaseDemandVO(demand);
        return Result.success(vo);
    }
}