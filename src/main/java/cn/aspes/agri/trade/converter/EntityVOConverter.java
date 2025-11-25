package cn.aspes.agri.trade.converter;

import cn.aspes.agri.trade.dto.ProductImageDTO;
import cn.aspes.agri.trade.entity.*;
import cn.aspes.agri.trade.service.ProductImageService;
import cn.aspes.agri.trade.vo.*;
import cn.aspes.agri.trade.service.OriginAreaService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Entity到VO转换器 - 使用ModelMapper
 */
@Component
@RequiredArgsConstructor
public class EntityVOConverter {
    
    private final ModelMapper modelMapper;
    private final ProductImageService productImageService;
    private final OriginAreaService originAreaService;

    // ============== User转换 ==============
    
    public UserVO toUserVO(User user) {
        if (user == null) {
            return null;
        }
        return modelMapper.map(user, UserVO.class);
    }
    
    public List<UserVO> toUserVOList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream()
                .map(this::toUserVO)
                .collect(Collectors.toList());
    }
    
    public IPage<UserVO> toUserVOPage(IPage<User> page) {
        if (page == null) {
            return null;
        }
        IPage<UserVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(toUserVOList(page.getRecords()));
        return result;
    }
    
    // ============== FarmerInfo转换 ==============
    
    public FarmerInfoVO toFarmerInfoVO(FarmerInfo farmerInfo) {
        if (farmerInfo == null) {
            return null;
        }

        FarmerInfoVO farmerInfoVO = new FarmerInfoVO();
        BeanUtils.copyProperties(farmerInfo, farmerInfoVO);
        
        // 查询并设置产地名称
        if (farmerInfo.getOriginAreaId() != null) {
            OriginArea originArea = originAreaService.getById(farmerInfo.getOriginAreaId());
            if (originArea != null) {
                farmerInfoVO.setOriginAreaName(originArea.getAreaName());
            }
        }

        return farmerInfoVO;
    }
    
    public List<FarmerInfoVO> toFarmerInfoVOList(List<FarmerInfo> farmerInfos) {
        if (farmerInfos == null || farmerInfos.isEmpty()) {
            return Collections.emptyList();
        }
        return farmerInfos.stream()
                .map(this::toFarmerInfoVO)
                .collect(Collectors.toList());
    }
    
    public IPage<FarmerInfoVO> toFarmerInfoVOPage(IPage<FarmerInfo> page) {
        if (page == null) {
            return null;
        }
        IPage<FarmerInfoVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(toFarmerInfoVOList(page.getRecords()));
        return result;
    }
    
    // ============== PurchaserInfo转换 ==============
    
    public PurchaserInfoVO toPurchaserInfoVO(PurchaserInfo purchaserInfo) {
        if (purchaserInfo == null) {
            return null;
        }
        
        PurchaserInfoVO vo = new PurchaserInfoVO();
        BeanUtils.copyProperties(purchaserInfo, vo);
        
        // 字段映射转换
        vo.setBusinessLicense(purchaserInfo.getBusinessLicenseUrl());
        vo.setLegalPerson(purchaserInfo.getLegalRepresentative());
        
        // 认证状态转换
        if (purchaserInfo.getAuditStatus() != null) {
            vo.setAuthStatus(purchaserInfo.getAuditStatus().ordinal());
        }
        
        return vo;
    }
    
    public List<PurchaserInfoVO> toPurchaserInfoVOList(List<PurchaserInfo> purchaserInfos) {
        if (purchaserInfos == null || purchaserInfos.isEmpty()) {
            return Collections.emptyList();
        }
        return purchaserInfos.stream()
                .map(this::toPurchaserInfoVO)
                .collect(Collectors.toList());
    }
    
    public IPage<PurchaserInfoVO> toPurchaserInfoVOPage(IPage<PurchaserInfo> page) {
        if (page == null) {
            return null;
        }
        IPage<PurchaserInfoVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(toPurchaserInfoVOList(page.getRecords()));
        return result;
    }
    
    // ============== FarmerProduct转换 ==============
    
    public FarmerProductVO toFarmerProductVO(FarmerProduct product) {
        if (product == null) {
            return null;
        }
        FarmerProductVO vo = modelMapper.map(product, FarmerProductVO.class);
        
        // 查询并设置产品图片
        List<ProductImage> images = productImageService.listByProductId(product.getId());
        if (images != null && !images.isEmpty()) {
            List<ProductImageDTO> imageDTOs = images.stream()
                    .sorted((a, b) -> a.getSort() != null && b.getSort() != null ? a.getSort().compareTo(b.getSort()) : 0)
                    .map(image -> {
                        ProductImageDTO dto = new ProductImageDTO();
                        dto.setUrl(image.getImageUrl());
                        dto.setImageType(image.getImageType());
                        dto.setSort(image.getSort());
                        return dto;
                    })
                    .collect(Collectors.toList());
            vo.setImages(imageDTOs);
        } else {
            vo.setImages(Collections.emptyList());
        }
        
        return vo;
    }
    
    public List<FarmerProductVO> toFarmerProductVOList(List<FarmerProduct> products) {
        if (products == null || products.isEmpty()) {
            return Collections.emptyList();
        }
        return products.stream()
                .map(this::toFarmerProductVO)
                .collect(Collectors.toList());
    }
    
    public IPage<FarmerProductVO> toFarmerProductVOPage(IPage<FarmerProduct> page) {
        if (page == null) {
            return null;
        }
        IPage<FarmerProductVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(toFarmerProductVOList(page.getRecords()));
        return result;
    }
    
    // ============== PurchaseDemand转换 ==============
    
    public PurchaseDemandVO toPurchaseDemandVO(PurchaseDemand demand) {
        if (demand == null) {
            return null;
        }
        return modelMapper.map(demand, PurchaseDemandVO.class);
    }
    
    public List<PurchaseDemandVO> toPurchaseDemandVOList(List<PurchaseDemand> demands) {
        if (demands == null || demands.isEmpty()) {
            return Collections.emptyList();
        }
        return demands.stream()
                .map(this::toPurchaseDemandVO)
                .collect(Collectors.toList());
    }
    
    public IPage<PurchaseDemandVO> toPurchaseDemandVOPage(IPage<PurchaseDemand> page) {
        if (page == null) {
            return null;
        }
        IPage<PurchaseDemandVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(toPurchaseDemandVOList(page.getRecords()));
        return result;
    }
    
    // ============== DockingRecord转换 ==============
    
    public DockingRecordVO toDockingRecordVO(DockingRecord record) {
        if (record == null) {
            return null;
        }
        return modelMapper.map(record, DockingRecordVO.class);
    }
    
    public List<DockingRecordVO> toDockingRecordVOList(List<DockingRecord> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        return records.stream()
                .map(this::toDockingRecordVO)
                .collect(Collectors.toList());
    }
    
    public IPage<DockingRecordVO> toDockingRecordVOPage(IPage<DockingRecord> page) {
        if (page == null) {
            return null;
        }
        IPage<DockingRecordVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(toDockingRecordVOList(page.getRecords()));
        return result;
    }
    
    // ============== PurchaseContract转换 ==============
    
    public PurchaseContractVO toPurchaseContractVO(PurchaseContract contract) {
        if (contract == null) {
            return null;
        }
        return modelMapper.map(contract, PurchaseContractVO.class);
    }
    
    public List<PurchaseContractVO> toPurchaseContractVOList(List<PurchaseContract> contracts) {
        if (contracts == null || contracts.isEmpty()) {
            return Collections.emptyList();
        }
        return contracts.stream()
                .map(this::toPurchaseContractVO)
                .collect(Collectors.toList());
    }
    
    public IPage<PurchaseContractVO> toPurchaseContractVOPage(IPage<PurchaseContract> page) {
        if (page == null) {
            return null;
        }
        IPage<PurchaseContractVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(toPurchaseContractVOList(page.getRecords()));
        return result;
    }
    
    // ============== PurchaseOrder转换 ==============
    
    public PurchaseOrderVO toPurchaseOrderVO(PurchaseOrder order) {
        if (order == null) {
            return null;
        }
        return modelMapper.map(order, PurchaseOrderVO.class);
    }
    
    public List<PurchaseOrderVO> toPurchaseOrderVOList(List<PurchaseOrder> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }
        return orders.stream()
                .map(this::toPurchaseOrderVO)
                .collect(Collectors.toList());
    }
    
    public IPage<PurchaseOrderVO> toPurchaseOrderVOPage(IPage<PurchaseOrder> page) {
        if (page == null) {
            return null;
        }
        IPage<PurchaseOrderVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(toPurchaseOrderVOList(page.getRecords()));
        return result;
    }
    
    // ============== PaymentRecord转换 ==============
    
    public PaymentRecordVO toPaymentRecordVO(PaymentRecord record) {
        if (record == null) {
            return null;
        }
        return modelMapper.map(record, PaymentRecordVO.class);
    }
    
    public List<PaymentRecordVO> toPaymentRecordVOList(List<PaymentRecord> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        return records.stream()
                .map(this::toPaymentRecordVO)
                .collect(Collectors.toList());
    }
    
    public IPage<PaymentRecordVO> toPaymentRecordVOPage(IPage<PaymentRecord> page) {
        if (page == null) {
            return null;
        }
        IPage<PaymentRecordVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(toPaymentRecordVOList(page.getRecords()));
        return result;
    }
    
    /**
     * 转换PaymentRecord分页对象（使用Page实现类）
     * @param page PaymentRecord分页对象
     * @return PaymentRecordVO分页对象
     */
    public Page<PaymentRecordVO> toPaymentRecordVOPage(Page<PaymentRecord> page) {
        if (page == null) {
            return null;
        }
        Page<PaymentRecordVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(toPaymentRecordVOList(page.getRecords()));
        return result;
    }
    
    // ============== LogisticsRecord转换 ==============
    
    public LogisticsVO toLogisticsVO(LogisticsRecord record) {
        if (record == null) {
            return null;
        }
        return modelMapper.map(record, LogisticsVO.class);
    }
    
    public List<LogisticsVO> toLogisticsVOList(List<LogisticsRecord> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        return records.stream()
                .map(this::toLogisticsVO)
                .collect(Collectors.toList());
    }
    
    public IPage<LogisticsVO> toLogisticsVOPage(IPage<LogisticsRecord> page) {
        if (page == null) {
            return null;
        }
        IPage<LogisticsVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(toLogisticsVOList(page.getRecords()));
        return result;
    }
    
    // ============== LogisticsTrace转换 ==============
    
    public LogisticsTraceVO toLogisticsTraceVO(LogisticsTrace trace) {
        if (trace == null) {
            return null;
        }
        return modelMapper.map(trace, LogisticsTraceVO.class);
    }
    
    public List<LogisticsTraceVO> toLogisticsTraceVOList(List<LogisticsTrace> traces) {
        if (traces == null || traces.isEmpty()) {
            return Collections.emptyList();
        }
        return traces.stream()
                .map(this::toLogisticsTraceVO)
                .collect(Collectors.toList());
    }
}