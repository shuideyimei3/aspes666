package cn.aspes.agri.trade.service.impl;

import cn.aspes.agri.trade.entity.ProductCategory;
import cn.aspes.agri.trade.exception.BusinessException;
import cn.aspes.agri.trade.mapper.ProductCategoryMapper;
import cn.aspes.agri.trade.service.ProductCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 产品分类服务实现类
 */
@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {
    
    @Override
    public List<ProductCategory> getTreeList() {
        return list(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getStatus, "active")
                .orderByAsc(ProductCategory::getCreateTime));
    }
    
    @Override
    public boolean checkNameUnique(String name, Long parentId, Long excludeId) {
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductCategory::getName, name)
                .eq(parentId != null, ProductCategory::getParentId, parentId)
                .isNull(parentId == null, ProductCategory::getParentId);
        if (excludeId != null) {
            wrapper.ne(ProductCategory::getId, excludeId);
        }
        return count(wrapper) == 0;
    }
    
    @Override
    public boolean save(ProductCategory entity) {
        if (!checkNameUnique(entity.getName(), entity.getParentId(), null)) {
            throw new BusinessException("同级分类名称已存在");
        }
        return super.save(entity);
    }
    
    @Override
    public boolean updateById(ProductCategory entity) {
        if (!checkNameUnique(entity.getName(), entity.getParentId(), entity.getId())) {
            throw new BusinessException("同级分类名称已存在");
        }
        return super.updateById(entity);
    }
}
