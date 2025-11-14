package cn.aspes.agri.trade.controller.b2c;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.converter.EntityVOConverter;
import cn.aspes.agri.trade.dto.LogisticsRequest;
import cn.aspes.agri.trade.dto.LogisticsTraceRequest;
import cn.aspes.agri.trade.entity.LogisticsRecord;
import cn.aspes.agri.trade.entity.LogisticsTrace;
import cn.aspes.agri.trade.service.LogisticsRecordService;
import cn.aspes.agri.trade.vo.LogisticsVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C端 - 物流跟踪控制器
 */
@Tag(name = "C端 - 物流跟踪")
@RestController
@RequestMapping("/api/c2c/logistics")
public class LogisticsController {
    
    @Resource
    private LogisticsRecordService logisticsRecordService;
    
    @Resource
    private EntityVOConverter entityVOConverter;
    
    @Operation(summary = "创建物流记录")
    @PostMapping
    public Result<Long> createLogistics(@Valid @RequestBody LogisticsRequest request) {
        Long logisticsId = logisticsRecordService.createLogistics(request);
        return Result.success(logisticsId);
    }
    
    @Operation(summary = "发货")
    @PutMapping("/{logisticsId}/ship")
    public Result<Void> shipGoods(@PathVariable Long logisticsId) {
        logisticsRecordService.shipGoods(logisticsId);
        return Result.success();
    }
    
    @Operation(summary = "添加物流轨迹")
    @PostMapping("/{logisticsId}/trace")
    public Result<Void> addTrace(@PathVariable Long logisticsId,
                                  @Valid @RequestBody LogisticsTraceRequest request) {
        logisticsRecordService.addTrace(logisticsId, request);
        return Result.success();
    }
    
    @Operation(summary = "确认签收")
    @PutMapping("/{logisticsId}/confirm")
    public Result<Void> confirmReceipt(@PathVariable Long logisticsId) {
        logisticsRecordService.confirmReceipt(logisticsId);
        return Result.success();
    }
    
    @Operation(summary = "查询订单物流信息")
    @GetMapping("/order/{orderId}")
    public Result<LogisticsVO> getByOrderId(@PathVariable Long orderId) {
        LogisticsRecord logistics = logisticsRecordService.getByOrderId(orderId);
        if (logistics == null) {
            return Result.success(null);
        }
        LogisticsVO vo = entityVOConverter.toLogisticsVO(logistics);
        return Result.success(vo);
    }
    
    @Operation(summary = "查询物流轨迹列表")
    @GetMapping("/{logisticsId}/traces")
    public Result<List<LogisticsTrace>> listTraces(@PathVariable Long logisticsId) {
        List<LogisticsTrace> traces = logisticsRecordService.listTraces(logisticsId);
        return Result.success(traces);
    }
    
    @Operation(summary = "分页查询物流轨迹")
    @GetMapping("/{logisticsId}/traces-page")
    public Result<Page<LogisticsTrace>> pageTraces(
            @PathVariable Long logisticsId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<LogisticsTrace> page = logisticsRecordService.pageTraces(logisticsId, current, size);
        return Result.success(page);
    }
}
