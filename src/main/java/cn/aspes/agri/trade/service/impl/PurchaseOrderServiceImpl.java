package cn.aspes.agri.trade.service.impl;

import cn.aspes.agri.trade.entity.FarmerProduct;
import cn.aspes.agri.trade.entity.PurchaseContract;
import cn.aspes.agri.trade.entity.PurchaseOrder;
import cn.aspes.agri.trade.entity.FarmerInfo;
import cn.aspes.agri.trade.entity.PurchaserInfo;
import cn.aspes.agri.trade.enums.ContractStatus;
import cn.aspes.agri.trade.enums.OrderStatus;
import cn.aspes.agri.trade.exception.BusinessException;
import cn.aspes.agri.trade.mapper.PurchaseOrderMapper;
import cn.aspes.agri.trade.service.FarmerProductService;
import cn.aspes.agri.trade.service.PurchaseContractService;
import cn.aspes.agri.trade.service.PurchaseOrderService;
import cn.aspes.agri.trade.service.StockReservationService;
import cn.aspes.agri.trade.service.FarmerInfoService;
import cn.aspes.agri.trade.service.PurchaserInfoService;
import cn.aspes.agri.trade.util.SnowflakeIdGenerator;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 采购订单服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> implements PurchaseOrderService {
    
    private final PurchaseContractService contractService;
    private final FarmerProductService productService;
    private final StockReservationService stockReservationService;
    private final SnowflakeIdGenerator idGenerator;
    private final StringRedisTemplate redisTemplate;
    private final FarmerInfoService farmerInfoService;
    private final PurchaserInfoService purchaserInfoService;
    
    private static final String STOCK_LOCK_KEY = "stock:lock:";
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrderFromContract(Long contractId) {
        // 查询合同
        PurchaseContract contract = contractService.getById(contractId);
        if (contract == null) {
            throw new BusinessException("合同不存在");
        }
        
        if (contract.getStatus() != ContractStatus.SIGNED) {
            throw new BusinessException("只有已签署的合同才能生成订单");
        }
        
        // 检查是否已生成订单
        long count = count(new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getContractId, contractId));
        if (count > 0) {
            throw new BusinessException("该合同已生成订单");
        }
        
        // 创建订单
        PurchaseOrder order = new PurchaseOrder();
        order.setId(idGenerator.nextId());
        order.setOrderNo("ORD" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss") + contractId);
        order.setContractId(contractId);
        order.setProductInfo(contract.getProductInfo());
        order.setStatus(OrderStatus.PENDING);
        
        save(order);
        
        // 创建订单后预留库存
        Map<String, Object> productInfo = contract.getProductInfo();
        if (productInfo != null && productInfo.containsKey("productId")) {
            try {
                Long productId = Long.valueOf(productInfo.get("productId").toString());
                Integer quantity = Integer.valueOf(productInfo.get("quantity").toString());
                stockReservationService.reserveStock(order.getId(), productId, quantity);
            } catch (Exception e) {
                // 库存预留失败不影响订单创建，只记录日志
                e.printStackTrace();
            }
        }
        
        // 更新合同状态
        contract.setStatus(ContractStatus.EXECUTING);
        contractService.updateById(contract);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inspectOrder(Long orderId, Integer actualQuantity, String inspectionResult) {
        PurchaseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("只有待交货状态的订单才能验收");
        }
        
        // ✅ 新增：验证合同状态，确保只有执行中的合同才能验收
        PurchaseContract contract = contractService.getById(order.getContractId());
        if (contract == null) {
            throw new BusinessException("关联合同不存在");
        }
        if (contract.getStatus() != ContractStatus.EXECUTING) {
            throw new BusinessException("只有执行中的合同才能进行验收，当前合同状态=" + contract.getStatus());
        }
        
        // 获取产品信息
        Map<String, Object> productInfo = order.getProductInfo();
        Long productId = Long.valueOf(productInfo.get("productId").toString());
        
        // ✅ 修复：订单验收时仅更新订单信息，不扣减库存
        // 库存扣减延迟到支付成功时，避免双重扣减
        BigDecimal unitPrice = new BigDecimal(productInfo.get("price").toString());
        order.setActualQuantity(actualQuantity);
        order.setActualAmount(unitPrice.multiply(new BigDecimal(actualQuantity)));
        order.setInspectionResult(inspectionResult);
        order.setDeliveryTime(LocalDateTime.now());
        order.setStatus(OrderStatus.DELIVERED);
        
        updateById(order);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long orderId) {
        PurchaseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException("只有已支付的订单才能完成");
        }
        
        order.setStatus(OrderStatus.COMPLETED);
        updateById(order);
        
        // 更新合同状态
        PurchaseContract contract = contractService.getById(order.getContractId());
        if (contract != null) {
            contract.setStatus(ContractStatus.COMPLETED);
            contractService.updateById(contract);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, String reason) {
        PurchaseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("已完成或已取消的订单不能取消");
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        updateById(order);
        
        // 取消订单时释放库存预留
        stockReservationService.releaseReservation(orderId, reason);
    }
    
    @Override
    public Page<PurchaseOrder> pageOrders(Integer current, Integer size, String status) {
        Page<PurchaseOrder> page = new Page<>(current, size);
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PurchaseOrder::getStatus, OrderStatus.valueOf(status.toUpperCase()));
        }
        
        wrapper.orderByDesc(PurchaseOrder::getCreateTime);
        return page(page, wrapper);
    }
    
    @Override
    public Page<PurchaseOrder> listMyOrders(Long userId, String role, Integer current, Integer size) {
        Page<PurchaseOrder> page = new Page<>(current, size);
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        
        // 根据角色查询订单
        if ("farmer".equalsIgnoreCase(role)) {
            // 农户查询：通过合同关联查询他作为卖方的订单
            FarmerInfo farmer = farmerInfoService.getByUserId(userId);
            if (farmer != null) {
                // ✅ 修复SQL注入：先查询合同ID列表，再用in查询
                List<PurchaseContract> contracts = contractService.list(
                        new LambdaQueryWrapper<PurchaseContract>()
                                .eq(PurchaseContract::getFarmerId, farmer.getId())
                                .select(PurchaseContract::getId)
                );
                if (!contracts.isEmpty()) {
                    List<Long> contractIds = contracts.stream()
                            .map(PurchaseContract::getId)
                            .collect(java.util.stream.Collectors.toList());
                    wrapper.in(PurchaseOrder::getContractId, contractIds);
                } else {
                    return page(page, new LambdaQueryWrapper<PurchaseOrder>().eq(PurchaseOrder::getId, -1L));
                }
            } else {
                return page(page, new LambdaQueryWrapper<PurchaseOrder>().eq(PurchaseOrder::getId, -1L));
            }
        } else if ("purchaser".equalsIgnoreCase(role)) {
            // 采购方查询：通过合同关联查询他作为买方的订单
            PurchaserInfo purchaser = purchaserInfoService.getByUserId(userId);
            if (purchaser != null) {
                // ✅ 修复SQL注入：先查询合同ID列表，再用in查询
                List<PurchaseContract> contracts = contractService.list(
                        new LambdaQueryWrapper<PurchaseContract>()
                                .eq(PurchaseContract::getPurchaserId, purchaser.getId())
                                .select(PurchaseContract::getId)
                );
                if (!contracts.isEmpty()) {
                    List<Long> contractIds = contracts.stream()
                            .map(PurchaseContract::getId)
                            .collect(java.util.stream.Collectors.toList());
                    wrapper.in(PurchaseOrder::getContractId, contractIds);
                } else {
                    return page(page, new LambdaQueryWrapper<PurchaseOrder>().eq(PurchaseOrder::getId, -1L));
                }
            } else {
                return page(page, new LambdaQueryWrapper<PurchaseOrder>().eq(PurchaseOrder::getId, -1L));
            }
        } else {
            // 其他角色返回空
            return page(page, new LambdaQueryWrapper<PurchaseOrder>().eq(PurchaseOrder::getId, -1L));
        }
        
        wrapper.orderByDesc(PurchaseOrder::getCreateTime);
        return page(page, wrapper);
    }
    
    @Override
    public PurchaseOrder getOrderDetail(Long orderId) {
        return getById(orderId);
    }
}
