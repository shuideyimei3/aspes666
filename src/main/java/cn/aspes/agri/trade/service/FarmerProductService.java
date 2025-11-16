package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.dto.FarmerProductRequest;
import cn.aspes.agri.trade.entity.FarmerProduct;
import cn.aspes.agri.trade.vo.FarmerProductVO;
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
    
    /**
     * 分页查询产品列表（包含图片信息）
     */
    IPage<FarmerProductVO> listProductsWithImages(int pageNum, int pageSize, Long categoryId, Integer originAreaId, String status);
    
    /**
     * 查询我的产品列表（包含图片信息）
     */
    IPage<FarmerProductVO> listMyProductsWithImages(Long farmerId, int pageNum, int pageSize);
    
    /**
     * 根据ID查询产品详情（包含图片信息）
     */
    FarmerProductVO getProductWithImagesById(Long productId);
    
    /**
     * 验证产品是否属于指定农户
     */
    boolean isProductOwner(Long productId, Long farmerId);
    
    /**
     * 根据产品ID获取产品信息
     * @param productId 产品ID
     * @return 产品信息
     */
    FarmerProduct getProductById(Long productId);
}