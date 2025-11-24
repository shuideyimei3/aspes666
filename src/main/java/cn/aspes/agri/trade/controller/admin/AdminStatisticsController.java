package cn.aspes.agri.trade.controller.admin;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.service.StatisticsService;
import cn.aspes.agri.trade.vo.StatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
/**
 * 后台管理 - 数据统计控制器
 */
@Tag(name = "后台管理 - 数据统计")
@RestController
@RequestMapping("/api/admin/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatisticsController {
    
    @Resource
    private StatisticsService statisticsService;
    
    @Operation(summary = "获取平台数据统计")
    @GetMapping("/platform")
    public Result<StatisticsVO.PlatformStats> getPlatformStats() {
        StatisticsVO.PlatformStats stats = statisticsService.getPlatformStats();
        return Result.success(stats);
    }
    
    @Operation(summary = "获取市级农户活跃度（最近N分钟）")
    @GetMapping("/farmer-activity")
    public Result<java.util.Map<String, Long>> getFarmerActivity(@org.springframework.web.bind.annotation.RequestParam(name = "windowMinutes", defaultValue = "5") int windowMinutes) {
        java.util.Map<String, Long> data = statisticsService.getFarmerActivityByCity(windowMinutes);
        return Result.success(data);
    }
    
    @Operation(summary = "SSE实时推送市级农户活跃度")
    @GetMapping("/farmer-activity/stream")
    public SseEmitter streamFarmerActivity(@org.springframework.web.bind.annotation.RequestParam(name = "windowMinutes", defaultValue = "5") int windowMinutes) {
        SseEmitter emitter = new SseEmitter(0L);
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    java.util.Map<String, Long> data = statisticsService.getFarmerActivityByCity(windowMinutes);
                    emitter.send(SseEmitter.event().name("activity").data(data));
                    Thread.sleep(3000);
                }
            } catch (Exception e) {
                try { emitter.completeWithError(e); } catch (Exception ignored) {}
            }
        });
        t.setDaemon(true);
        t.start();
        return emitter;
    }
}