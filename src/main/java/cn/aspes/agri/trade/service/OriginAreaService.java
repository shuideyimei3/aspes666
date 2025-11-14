package cn.aspes.agri.trade.service;

import cn.aspes.agri.trade.entity.OriginArea;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 产地信息服务接口
 */
public interface OriginAreaService extends IService<OriginArea> {
    
    /**
     * 分页查询产地（支持省市筛选）
     */
    Page<OriginArea> pageQuery(Integer current, Integer size, String province, String city, Boolean isPovertyArea);
    
    /**
     * 验证产地编码唯一性
     */
    boolean checkAreaCodeUnique(String areaCode, Integer excludeId);
}
