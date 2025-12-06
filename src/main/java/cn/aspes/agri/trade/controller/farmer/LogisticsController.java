package cn.aspes.agri.trade.controller.farmer;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.converter.EntityVOConverter;
import cn.aspes.agri.trade.dto.LogisticsRequest;
import cn.aspes.agri.trade.dto.LogisticsTraceRequest;
import cn.aspes.agri.trade.entity.LogisticsRecord;
import cn.aspes.agri.trade.entity.LogisticsTrace;
import cn.aspes.agri.trade.security.CustomUserDetails;
import cn.aspes.agri.trade.service.LogisticsRecordService;
import cn.aspes.agri.trade.vo.LogisticsVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C端 - 物流跟踪控制器
 */
@Tag(name = "C端 - 物流跟踪")
@RestController
@RequestMapping("/api/farmer/logistics")
public class LogisticsController {
    
    @Resource
    private LogisticsRecordService logisticsRecordService;
    
    @Resource
    private EntityVOConverter entityVOConverter;
    
    @Operation(summary = "创建物流记录")
    @PostMapping
    @PreAuthorize("hasAnyRole('FARMER', 'PURCHASER', 'ADMIN')")
    public Result<Long> createLogistics(@Valid @RequestBody LogisticsRequest request,
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long logisticsId = logisticsRecordService.createLogistics(request, userDetails.getId());
        return Result.success(logisticsId);
    }
    
    @Operation(summary = "添加物流轨迹")
    @PostMapping("/{logisticsId}/trace")
    @PreAuthorize("hasAnyRole('FARMER', 'PURCHASER', 'ADMIN')")
    public Result<Void> addTrace(@PathVariable Long logisticsId,
                                  @Valid @RequestBody LogisticsTraceRequest request,
                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        logisticsRecordService.addTrace(logisticsId, request, userDetails.getId());
        return Result.success();
    }
    
    @Operation(summary = "确认签收")
    @PutMapping("/{logisticsId}/confirm")
    @PreAuthorize("hasAnyRole('PURCHASER')")
    public Result<Void> confirmReceipt(@PathVariable Long logisticsId,
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        logisticsRecordService.confirmReceipt(logisticsId, userDetails.getId());
        return Result.success();
    }
    
    @Operation(summary = "查询订单物流信息")
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('FARMER', 'PURCHASER', 'ADMIN')")
    public Result<LogisticsVO> getByOrderId(@PathVariable Long orderId,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        LogisticsRecord logistics = logisticsRecordService.getByOrderId(orderId, userDetails.getId());
        LogisticsVO vo = entityVOConverter.toLogisticsVO(logistics);
        return Result.success(vo);
    }
    
    @Operation(summary = "查询订单的所有物流记录")
    @GetMapping("/order/{orderId}/all")
    @PreAuthorize("hasAnyRole('FARMER', 'PURCHASER', 'ADMIN')")
    public Result<List<LogisticsVO>> listByOrderId(@PathVariable Long orderId,
                                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<LogisticsRecord> logisticsList = logisticsRecordService.listByOrderId(orderId, userDetails.getId());
        List<LogisticsVO> voList = logisticsList.stream()
                .map(entityVOConverter::toLogisticsVO)
                .collect(java.util.stream.Collectors.toList());
        return Result.success(voList);
    }
    
    @Operation(summary = "查询物流轨迹列表")
    @GetMapping("/{logisticsId}/traces")
    @PreAuthorize("hasAnyRole('FARMER', 'PURCHASER', 'ADMIN')")
    public Result<List<LogisticsTrace>> listTraces(@PathVariable Long logisticsId,
                                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<LogisticsTrace> traces = logisticsRecordService.listTraces(logisticsId, userDetails.getId());
        return Result.success(traces);
    }
    
    @Operation(summary = "分页查询物流轨迹")
    @GetMapping("/{logisticsId}/traces-page")
    @PreAuthorize("hasAnyRole('FARMER', 'PURCHASER', 'ADMIN')")
    public Result<Page<LogisticsTrace>> pageTraces(
            @PathVariable Long logisticsId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Page<LogisticsTrace> page = logisticsRecordService.pageTraces(logisticsId, current, size, userDetails.getId());
        return Result.success(page);
    }
}