package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.dto.DockingFeedbackRequest;
import cn.aspes.agri.trade.dto.DockingRecordRequest;
import cn.aspes.agri.trade.entity.DockingRecord;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 对接记录服务
 */
public interface DockingRecordService extends IService<DockingRecord> {
    
    /**
     * 农户响应需求
     */
    Long respondToDemand(Long farmerId, DockingRecordRequest request);
    
    /**
     * 采购方处理对接
     */
    void handleDocking(Long dockingId, Long purchaserId, DockingFeedbackRequest request);
    
    /**
     * 查询需求的对接列表
     */
    IPage<DockingRecord> listByDemand(Long demandId, int pageNum, int pageSize);
    
    /**
     * 查询我的对接记录
     */
    IPage<DockingRecord> listMyDockings(Long userId, String role, int pageNum, int pageSize);
}
