package cn.aspes.agri.trade.service.impl;

import cn.aspes.agri.trade.dto.ContractRequest;
import cn.aspes.agri.trade.entity.*;
import cn.aspes.agri.trade.enums.ContractStatus;
import cn.aspes.agri.trade.enums.DockingStatus;
import cn.aspes.agri.trade.enums.OrderStatus;
import cn.aspes.agri.trade.exception.BusinessException;
import cn.aspes.agri.trade.mapper.PurchaseContractMapper;
import cn.aspes.agri.trade.mapper.PurchaseOrderMapper;
import cn.aspes.agri.trade.service.*;
import cn.aspes.agri.trade.util.ProductSnapshotUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 采购合同服务实现类
 */
@Slf4j
@Service
public class PurchaseContractServiceImpl extends ServiceImpl<PurchaseContractMapper, PurchaseContract> implements PurchaseContractService {
    
    @Resource
    private DockingRecordService dockingRecordService;
    
    @Resource
    private FarmerInfoService farmerInfoService;
    
    @Resource
    private PurchaserInfoService purchaserInfoService;
    
    // ✅ 修复：移除PurchaseOrderService依赖，改用PurchaseOrderMapper防止循环依赖
    @Resource
    private PurchaseOrderMapper purchaseOrderMapper;
    
    @Resource
    private StockReservationService stockReservationService;
    
    @Resource
    private FileUploadService fileUploadService;
    
    @Resource
    private FarmerProductService farmerProductService;
    
    @Resource
    private ProductSnapshotUtil productSnapshotUtil;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createContract(Long userId, ContractRequest request) {
        // 检查对接记录是否存在且已达成
        DockingRecord docking = dockingRecordService.getById(request.getDockingId());
        if (docking == null) {
            throw new BusinessException("对接记录不存在");
        }
        if (docking.getStatus() != DockingStatus.AGREED) {
            throw new BusinessException("只能基于已达成的对接记录生成合同");
        }
        
        // 检查是否已生成合同
        long count = count(new LambdaQueryWrapper<PurchaseContract>()
                .eq(PurchaseContract::getDockingId, request.getDockingId()));
        if (count > 0) {
            throw new BusinessException("该对接记录已生成合同，请勿重复操作");
        }
        
        // 获取采购方和农户信息
        PurchaserInfo purchaserInfo = purchaserInfoService.getByUserId(userId);
        if (purchaserInfo == null) {
            throw new BusinessException("采购方信息不存在");
        }
        
        FarmerInfo farmerInfo = farmerInfoService.getById(docking.getFarmerId());
        
        // 根据产品ID获取产品信息
        FarmerProduct product = farmerProductService.getProductById(request.getProductId());
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        
        // 创建产品信息快照
        Map<String, Object> productSnapshot = productSnapshotUtil.createProductSnapshot(product);
        
        // 从请求中获取数量
        Integer quantity = request.getQuantity();
        
        // 计算合同总金额
        BigDecimal price = product.getPrice();
        BigDecimal totalAmount = price.multiply(new BigDecimal(quantity));
        
        // 生成合同编号：C + 日期 + 四位流水号
        String contractNo = generateContractNo();
        
        // 创建合同
        PurchaseContract contract = new PurchaseContract();
        BeanUtils.copyProperties(request, contract);
        contract.setContractNo(contractNo);
        contract.setPurchaserId(purchaserInfo.getId());
        contract.setFarmerId(docking.getFarmerId());
        contract.setProductId(request.getProductId());
        contract.setProductInfo(productSnapshot);
        contract.setTotalAmount(totalAmount);
        contract.setStatus(ContractStatus.DRAFT);
        
        save(contract);
        return contract.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void signContract(Long contractId, Long userId, MultipartFile signFile, String role) {
        PurchaseContract contract = getById(contractId);
        if (contract == null) {
            throw new BusinessException("合同不存在");
        }
        
        if (contract.getStatus() == ContractStatus.SIGNED) {
            throw new BusinessException("合同已签署，请勿重复操作");
        }
        
        // 校验签字文件
        if (signFile == null || signFile.isEmpty()) {
            throw new BusinessException("签署时必须提供签字文件");
        }
        
        // 上传签字文件
        String signUrl = fileUploadService.uploadContractSign(signFile);
        
        // 根据角色更新签字文件
        if ("farmer".equalsIgnoreCase(role)) {
            FarmerInfo farmerInfo = farmerInfoService.getByUserId(userId);
            if (!contract.getFarmerId().equals(farmerInfo.getId())) {
                throw new BusinessException("无权签署该合同");
            }
            contract.setFarmerSignUrl(signUrl);
        } else if ("purchaser".equalsIgnoreCase(role)) {
            PurchaserInfo purchaserInfo = purchaserInfoService.getByUserId(userId);
            if (!contract.getPurchaserId().equals(purchaserInfo.getId())) {
                throw new BusinessException("无权签署该合同");
            }
            contract.setPurchaserSignUrl(signUrl);
        }
        
        // 如果双方都已签署，更新状态为已签署
        if (contract.getFarmerSignUrl() != null && contract.getPurchaserSignUrl() != null) {
            contract.setStatus(ContractStatus.SIGNED);
        }
        
        updateById(contract);
    }
    
    @Override
    public IPage<PurchaseContract> listMyContracts(Long userId, String role, int pageNum, int pageSize) {
        Page<PurchaseContract> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PurchaseContract> wrapper = new LambdaQueryWrapper<>();
        
        if ("farmer".equalsIgnoreCase(role)) {
            FarmerInfo farmerInfo = farmerInfoService.getByUserId(userId);
            wrapper.eq(PurchaseContract::getFarmerId, farmerInfo.getId());
        } else if ("purchaser".equalsIgnoreCase(role)) {
            PurchaserInfo purchaserInfo = purchaserInfoService.getByUserId(userId);
            wrapper.eq(PurchaseContract::getPurchaserId, purchaserInfo.getId());
        }
        
        wrapper.orderByDesc(PurchaseContract::getCreateTime);
        return page(page, wrapper);
    }
    
    /**
     * 生成合同编号：C + YYYYMMDD + 4位流水号
     */
    private String generateContractNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "C" + dateStr;
        
        // 查询当天最大的流水号
        LambdaQueryWrapper<PurchaseContract> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(PurchaseContract::getContractNo, prefix);
        wrapper.orderByDesc(PurchaseContract::getContractNo);
        wrapper.last("LIMIT 1");
        
        PurchaseContract last = getOne(wrapper);
        int sequence = 1;
        if (last != null && last.getContractNo() != null) {
            String lastNo = last.getContractNo().substring(prefix.length());
            sequence = Integer.parseInt(lastNo) + 1;
        }
        return prefix + String.format("%04d", sequence);
    }
    /**
     * 撒回合同
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawContract(Long contractId, Long userId, String reason) {
        PurchaseContract contract = getById(contractId);
        if (contract == null) {
            throw new BusinessException("合同不存在");
        }
        
        if (contract.getStatus() != ContractStatus.DRAFT) {
            throw new BusinessException("只有草稿中的合同才能撤回");
        }
        
        contract.setStatus(ContractStatus.TERMINATED);
        updateById(contract);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectContract(Long contractId, Long userId, String reason) {
        PurchaseContract contract = getById(contractId);
        if (contract == null) {
            throw new BusinessException("合同不存在");
        }
        
        if (contract.getStatus() != ContractStatus.SIGNED) {
            throw new BusinessException("只有已签署的合同才能拒签");
        }
        
        contract.setStatus(ContractStatus.TERMINATED);
        updateById(contract);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void terminateContract(Long contractId, Long userId, String reason) {
        PurchaseContract contract = getById(contractId);
        if (contract == null) {
            throw new BusinessException("合同不存在");
        }
        
        if (contract.getStatus() == ContractStatus.COMPLETED || contract.getStatus() == ContractStatus.TERMINATED) {
            throw new BusinessException("已完成或已终止的合同无法终止");
        }
        
        contract.setStatus(ContractStatus.TERMINATED);
        updateById(contract);
        
        // ✅ 修复：合同终止时，需要释放其下所有订单的库存预留
        // 改用Mapper直接查询，避免循环依赖
        try {
            List<PurchaseOrder> orders = purchaseOrderMapper.selectList(
                    new LambdaQueryWrapper<PurchaseOrder>()
                            .eq(PurchaseOrder::getContractId, contractId)
                            .ne(PurchaseOrder::getStatus, OrderStatus.COMPLETED)
                            .ne(PurchaseOrder::getStatus, OrderStatus.CANCELLED)
            );
            
            for (PurchaseOrder order : orders) {
                try {
                    stockReservationService.releaseReservation(order.getId(), "合同终止释放");
                } catch (Exception e) {
                    log.warn("释放合同下订单的库存预留失败，订单={}", order.getId(), e);
                }
            }
        } catch (Exception e) {
            log.warn("下查合同订单时出错，合同={}", contractId, e);
        }
    }
    
    @Override
    public Page<PurchaseContract> pageContracts(Integer current, Integer size, String status) {
        Page<PurchaseContract> page = new Page<>(current, size);
        LambdaQueryWrapper<PurchaseContract> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PurchaseContract::getStatus, ContractStatus.valueOf(status.toUpperCase()));
        }
        
        wrapper.orderByDesc(PurchaseContract::getCreateTime);
        return page(page, wrapper);
    }
    
    @Override
    public PurchaseContract getContractDetail(Long contractId) {
        return getById(contractId);
    }
}