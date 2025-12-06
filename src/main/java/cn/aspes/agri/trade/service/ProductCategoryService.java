package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.entity.ProductCategory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 产品分类服务接口
 */
public interface ProductCategoryService extends IService<ProductCategory> {
    
    /**
     * 获取树形分类结构
     */
    List<ProductCategory> getTreeList();
    
    /**
     * 验证分类名唯一性
     */
    boolean checkNameUnique(String name, Long parentId, Long excludeId);
}
