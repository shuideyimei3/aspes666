package cn.aspes.agri.trade.service.impl;

import cn.aspes.agri.trade.entity.OriginArea;
import cn.aspes.agri.trade.exception.BusinessException;
import cn.aspes.agri.trade.mapper.OriginAreaMapper;
import cn.aspes.agri.trade.service.OriginAreaService;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

/**
 * 产地信息服务实现类
 */
@Service
public class OriginAreaServiceImpl extends ServiceImpl<OriginAreaMapper, OriginArea> implements OriginAreaService {
    
    @Override
    @Cacheable(value = "originAreas", key = "'list'")
    public List<OriginArea> list() {
        return super.list();
    }
    
    @Override
    @Cacheable(value = "originAreas", key = "'id:' + #id")
    public OriginArea getById(Serializable id) {
        return super.getById(id);
    }
    
    @Override
    @Cacheable(value = "originAreas", key = "'page:' + #current + ':' + #size + ':' + #province + ':' + #city + ':' + #isPovertyArea")
    public Page<OriginArea> pageQuery(Integer current, Integer size, String province, String city, Boolean isPovertyArea) {
        Page<OriginArea> page = new Page<>(current, size);
        LambdaQueryWrapper<OriginArea> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(province), OriginArea::getProvince, province)
                .eq(StrUtil.isNotBlank(city), OriginArea::getCity, city)
                .eq(isPovertyArea != null, OriginArea::getIsPovertyArea, isPovertyArea)
                .orderByDesc(OriginArea::getCreateTime);
        return page(page, wrapper);
    }
    
    @Override
    public boolean checkAreaCodeUnique(String areaCode, Integer excludeId) {
        LambdaQueryWrapper<OriginArea> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OriginArea::getAreaCode, areaCode);
        if (excludeId != null) {
            wrapper.ne(OriginArea::getAreaId, excludeId);
        }
        return count(wrapper) == 0;
    }
    
    @Override
    @CacheEvict(value = "originAreas", allEntries = true)
    public boolean save(OriginArea entity) {
        if (!checkAreaCodeUnique(entity.getAreaCode(), null)) {
            throw new BusinessException("产地编码已存在");
        }
        return super.save(entity);
    }
    
    @Override
    @CacheEvict(value = "originAreas", allEntries = true)
    public boolean updateById(OriginArea entity) {
        if (!checkAreaCodeUnique(entity.getAreaCode(), entity.getAreaId())) {
            throw new BusinessException("产地编码已存在");
        }
        return super.updateById(entity);
    }
    
    @Override
    @CacheEvict(value = "originAreas", allEntries = true)
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }
}