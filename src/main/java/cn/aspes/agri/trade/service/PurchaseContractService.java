package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.dto.ContractRequest;
import cn.aspes.agri.trade.entity.PurchaseContract;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 采购合同服务接口
 */
public interface PurchaseContractService extends IService<PurchaseContract> {
    
    /**
     * 创建合同
     */
    Long createContract(Long userId, ContractRequest request);
    
    /**
     * 签署合同
     */
    void signContract(Long contractId, Long userId, ContractRequest request, String role);
    
    /**
     * 查询我的合同列表
     */
    IPage<PurchaseContract> listMyContracts(Long userId, String role, int pageNum, int pageSize);
    
    /**
     * 撤回合同
     */
    void withdrawContract(Long contractId, Long userId, String reason);
    
    /**
     * 拒签合同
     */
    void rejectContract(Long contractId, Long userId, String reason);
    
    /**
     * 终止合同
     */
    void terminateContract(Long contractId, Long userId, String reason);
    
    /**
     * 分页查询合同
     */
    Page<PurchaseContract> pageContracts(Integer current, Integer size, String status);
    
    /**
     * 合同详情
     */
    PurchaseContract getContractDetail(Long contractId);
}
