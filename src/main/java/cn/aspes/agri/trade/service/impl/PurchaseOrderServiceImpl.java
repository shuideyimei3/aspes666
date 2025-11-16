package cn.aspes.agri.trade.service.impl;

import cn.aspes.agri.trade.entity.FarmerProduct;
import cn.aspes.agri.trade.entity.PurchaseContract;
import cn.aspes.agri.trade.entity.PurchaseOrder;
import cn.aspes.agri.trade.entity.FarmerInfo;
import cn.aspes.agri.trade.entity.PurchaserInfo;
import cn.aspes.agri.trade.entity.PaymentRecord;
import cn.aspes.agri.trade.enums.ContractStatus;
import cn.aspes.agri.trade.enums.OrderStatus;
import cn.aspes.agri.trade.enums.PaymentStatus;
import cn.aspes.agri.trade.exception.BusinessException;
import cn.aspes.agri.trade.mapper.PurchaseOrderMapper;
import cn.aspes.agri.trade.mapper.PurchaseContractMapper;
import cn.aspes.agri.trade.service.FarmerProductService;
import cn.aspes.agri.trade.service.PurchaseContractService;
import cn.aspes.agri.trade.service.PurchaseOrderService;
import cn.aspes.agri.trade.service.PaymentRecordService;
import cn.aspes.agri.trade.service.StockReservationService;
import cn.aspes.agri.trade.service.FarmerInfoService;
import cn.aspes.agri.trade.service.PurchaserInfoService;
import cn.aspes.agri.trade.util.ProductSnapshotUtil;
import cn.aspes.agri.trade.util.SnowflakeIdGenerator;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> implements PurchaseOrderService, ApplicationContextAware {
    
    private ApplicationContext applicationContext;
    
    private final PurchaseContractService contractService;
    private final FarmerProductService productService;
    private final StockReservationService stockReservationService;
    private final SnowflakeIdGenerator idGenerator;
    private final StringRedisTemplate redisTemplate;
    private final FarmerInfoService farmerInfoService;
    private final PurchaserInfoService purchaserInfoService;
    private final ProductSnapshotUtil productSnapshotUtil;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseContractMapper contractMapper;
    
    private static final String STOCK_LOCK_KEY = "stock:lock:";
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    /**
     * 获取支付记录服务（延迟加载，避免循环依赖）
     */
    private PaymentRecordService getPaymentRecordService() {
        return applicationContext.getBean(PaymentRecordService.class);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrder createOrderFromContract(Long contractId) {
        log.info("从合同创建订单，合同ID: {}", contractId);
        
        // 1. 获取合同信息
        PurchaseContract contract = contractService.getById(contractId);
        if (contract == null) {
            throw new BusinessException("合同不存在");
        }
        
        if (!ContractStatus.SIGNED.equals(contract.getStatus())) {
            throw new BusinessException("合同未签署，无法创建订单");
        }
        
        // 检查是否已存在订单
        LambdaQueryWrapper<PurchaseOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchaseOrder::getContractId, contractId);
        PurchaseOrder existingOrder = getOne(queryWrapper);
        if (existingOrder != null) {
            throw new BusinessException("该合同已创建订单");
        }
        
        // 2. 根据productId获取产品信息
        FarmerProduct product = productService.getProductById(contract.getProductId());
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        
        // 3. 创建产品快照
        Map<String, Object> productInfo = productSnapshotUtil.createProductSnapshot(product);
        
        // 4. 创建订单
        PurchaseOrder order = new PurchaseOrder();
        order.setId(idGenerator.nextId());
        order.setOrderNo(generateOrderNo());
        order.setContractId(contractId);
        order.setProductId(contract.getProductId());
        order.setProductInfo(productInfo);
        order.setQuantity(contract.getQuantity());
        order.setTotalAmount(contract.getTotalAmount());
        order.setFarmerId(product.getFarmerId());
        order.setPurchaserId(contract.getPurchaserId());
        order.setStatus(OrderStatus.PENDING_INSPECTION);
        order.setRemark("从合同创建订单");
        
        // 5. 先保存订单到数据库
        boolean saved = save(order);
        if (!saved) {
            throw new BusinessException("订单创建失败");
        }
        
        // 6. 再预留库存
        boolean stockReserved = stockReservationService.reserveStock(
            product.getId(), 
            contract.getQuantity(), 
            order.getId()
        );
        
        if (!stockReserved) {
            // 如果库存预留失败，删除已创建的订单
            removeById(order.getId());
            throw new BusinessException("库存不足，无法创建订单");
        }
        
        // 7. 更新合同状态为执行中
        contract.setStatus(ContractStatus.EXECUTING);
        contractService.updateById(contract);
        
        log.info("订单创建成功，订单ID: {}, 订单号: {}, 合同状态已更新为执行中", order.getId(), order.getOrderNo());
        return order;
    }
    

    
    /**
     * 订单完成
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long orderId) {
        // 1. 查询订单
        PurchaseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 2. 验证订单状态
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException("只有已交货的订单才能完成，当前订单状态：" + order.getStatus().getDesc());
        }
        
        // 3. 验证订单是否已支付全部金额
        List<PaymentRecord> paymentRecords = getPaymentRecordService().listByOrder(orderId);
        if (paymentRecords == null || paymentRecords.isEmpty()) {
            throw new BusinessException("订单尚未支付，无法完成");
        }
        
        // 计算已支付金额
        BigDecimal paidAmount = paymentRecords.stream()
            .filter(record -> record.getStatus() == PaymentStatus.SUCCESS)
            .map(PaymentRecord::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 比较已支付金额与订单总金额
        if (paidAmount.compareTo(order.getTotalAmount()) < 0) {
            throw new BusinessException("订单尚未支付全部金额，已支付：" + paidAmount + "，需支付：" + order.getTotalAmount());
        }
        
        // 4. 查询合同
        PurchaseContract contract = contractService.getById(order.getContractId());
        if (contract == null) {
            throw new BusinessException("关联合同不存在");
        }
        
        // 5. 更新订单状态
        order.setStatus(OrderStatus.COMPLETED);
        updateById(order);
        
        // 6. 检查合同下所有订单是否都已完成，如果是，则更新合同状态为已完成
        List<PurchaseOrder> contractOrders = list(
            new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getContractId, contract.getId())
        );
        
        boolean allCompleted = contractOrders.stream()
            .allMatch(o -> o.getStatus() == OrderStatus.COMPLETED || o.getStatus() == OrderStatus.CANCELLED);
            
        if (allCompleted) {
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
        // 使用productId字段而不是从productInfo中获取
        stockReservationService.releaseStock(order.getProductId(), orderId);
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
    
    /**
     * 生成订单号
     * 格式: ORD + 年月日时分秒 + 4位随机数
     */
    private String generateOrderNo() {
        String timestamp = DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        int randomNum = (int) (Math.random() * 9000) + 1000; // 生成4位随机数
        return "ORD" + timestamp + randomNum;
    }
    
    /**
     * 农户交货
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deliverOrder(Long orderId, Integer actualQuantity, String inspectionResult) {
        // 1. 查询订单
        PurchaseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 2. 验证订单状态
        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException("只有已支付的订单才能交货，当前订单状态：" + order.getStatus().getDesc());
        }
        
        // 3. 查询合同
        PurchaseContract contract = contractService.getById(order.getContractId());
        if (contract == null) {
            throw new BusinessException("关联合同不存在");
        }
        
        // 4. 验证合同状态
        if (contract.getStatus() != ContractStatus.EXECUTING) {
            throw new BusinessException("只有执行中的合同才能交货，当前合同状态：" + contract.getStatus().getDesc());
        }
        
        // 5. 更新订单信息
        order.setActualQuantity(actualQuantity);
        order.setInspectionResult(inspectionResult);
        order.setDeliveryTime(LocalDateTime.now());
        order.setStatus(OrderStatus.DELIVERED);
        updateById(order);
    }
}