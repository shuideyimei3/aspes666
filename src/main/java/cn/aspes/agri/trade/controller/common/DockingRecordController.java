package cn.aspes.agri.trade.controller.common;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.converter.EntityVOConverter;
import cn.aspes.agri.trade.dto.DockingFeedbackRequest;
import cn.aspes.agri.trade.dto.DockingRecordRequest;
import cn.aspes.agri.trade.entity.DockingRecord;
import cn.aspes.agri.trade.enums.UserRole;
import cn.aspes.agri.trade.security.CustomUserDetails;
import cn.aspes.agri.trade.service.DockingRecordService;
import cn.aspes.agri.trade.service.FarmerInfoService;
import cn.aspes.agri.trade.service.PurchaserInfoService;
import cn.aspes.agri.trade.vo.DockingRecordVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 共享 - 对接记录管理控制器
 */
@Tag(name = "共享 - 对接记录")
@RestController
@RequestMapping("/api/shared/dockings")
public class DockingRecordController {
    
    @Resource
    private DockingRecordService dockingRecordService;
    
    @Resource
    private FarmerInfoService farmerInfoService;
    
    @Resource
    private PurchaserInfoService purchaserInfoService;
    
    @Resource
    private EntityVOConverter entityVOConverter;
    
    @Operation(summary = "农户响应需求")
    @PostMapping("/respond")
    public Result<Long> respondToDemand(@AuthenticationPrincipal CustomUserDetails userDetails,
                                         @Valid @RequestBody DockingRecordRequest request) {
        Long farmerId = farmerInfoService.getByUserId(userDetails.getId()).getId();
        Long dockingId = dockingRecordService.respondToDemand(farmerId, request);
        return Result.success(dockingId);
    }
    
    @Operation(summary = "采购方处理对接")
    @PutMapping("/{dockingId}/handle")
    public Result<Void> handleDocking(@PathVariable Long dockingId,
                                       @AuthenticationPrincipal CustomUserDetails userDetails,
                                       @Valid @RequestBody DockingFeedbackRequest request) {
        Long purchaserId = purchaserInfoService.getByUserId(userDetails.getId()).getId();
        dockingRecordService.handleDocking(dockingId, purchaserId, request);
        return Result.success();
    }
    
    @Operation(summary = "查询需求的对接列表")
    @GetMapping("/demand/{demandId}")
    public Result<IPage<DockingRecordVO>> listByDemand(@PathVariable Long demandId,
                                                      @RequestParam(defaultValue = "1") int pageNum,
                                                      @RequestParam(defaultValue = "10") int pageSize) {
        IPage<DockingRecord> page = dockingRecordService.listByDemand(demandId, pageNum, pageSize);
        IPage<DockingRecordVO> voPage = entityVOConverter.toDockingRecordVOPage(page);
        return Result.success(voPage);
    }
    
    @Operation(summary = "查询我的对接记录")
    @GetMapping("/my")
    public Result<IPage<DockingRecordVO>> listMyDockings(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                        @RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        String role = userDetails.getRole() == UserRole.FARMER ? "farmer" : "purchaser";
        Long userId = userDetails.getId();
        IPage<DockingRecord> page = dockingRecordService.listMyDockings(userId, role, pageNum, pageSize);
        IPage<DockingRecordVO> voPage = entityVOConverter.toDockingRecordVOPage(page);
        return Result.success(voPage);
    }
}