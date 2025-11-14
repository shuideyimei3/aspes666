package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.dto.FarmerProductRequest;
import cn.aspes.agri.trade.entity.FarmerProduct;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 农户产品服务接口
 */
public interface FarmerProductService extends IService<FarmerProduct> {
    
    /**
     * 发布产品
     */
    Long publishProduct(Long farmerId, FarmerProductRequest request);
    
    /**
     * 更新产品
     */
    void updateProduct(Long productId, Long farmerId, FarmerProductRequest request);
    
    /**
     * 产品上架
     */
    void onSale(Long productId, Long farmerId);
    
    /**
     * 产品下架
     */
    void offSale(Long productId, Long farmerId);
    
    /**
     * 分页查询产品列表
     */
    IPage<FarmerProduct> listProducts(int pageNum, int pageSize, Long categoryId, Integer originAreaId, String status);
    
    /**
     * 查询我的产品列表
     */
    IPage<FarmerProduct> listMyProducts(Long farmerId, int pageNum, int pageSize);
}
