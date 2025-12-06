package cn.aspes.agri.trade.mapper;

import cn.aspes.agri.trade.entity.FarmerProduct;
import cn.aspes.agri.trade.vo.FarmerProductVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FarmerProductMapper extends BaseMapper<FarmerProduct> {
    
    /**
     * 分页查询产品列表（包含图片信息）
     */
    IPage<FarmerProductVO> selectProductsWithImages(Page<FarmerProductVO> page, 
                                                              @Param("categoryId") Long categoryId,
                                                              @Param("originAreaId") Integer originAreaId,
                                                              @Param("status") String status);
    
    /**
     * 查询我的产品列表（包含图片信息）
     */
    IPage<FarmerProductVO> selectMyProductsWithImages(Page<FarmerProductVO> page, 
                                                                 @Param("farmerId") Long farmerId);
    
    /**
     * 根据ID查询产品详情（包含图片信息）
     */
    FarmerProductVO selectProductWithImagesById(@Param("productId") Long productId);
}