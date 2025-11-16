package cn.aspes.agri.trade.util;

import cn.aspes.agri.trade.entity.FarmerProduct;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 产品信息快照工具类
 * 用于在创建合同时保存产品信息的快照，确保历史数据不受后续产品信息变更影响
 */
@Component
@Slf4j
public class ProductSnapshotUtil {
    
    private final ObjectMapper objectMapper;
    
    public ProductSnapshotUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * 将FarmerProduct对象转换为产品信息快照Map
     * @param product 产品信息
     * @return 产品信息快照Map
     */
    public Map<String, Object> createProductSnapshot(FarmerProduct product) {
        if (product == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> snapshot = new HashMap<>();
        
        // 基本信息
        snapshot.put("id", product.getId());
        snapshot.put("name", product.getName());
        snapshot.put("spec", product.getSpec());
        snapshot.put("unit", product.getUnit());
        snapshot.put("price", product.getPrice());
        snapshot.put("minPurchase", product.getMinPurchase());
        snapshot.put("stock", product.getStock());
        snapshot.put("status", product.getStatus());
        
        // 关联信息
        snapshot.put("farmerId", product.getFarmerId());
        snapshot.put("categoryId", product.getCategoryId());
        snapshot.put("originAreaId", product.getOriginAreaId());
        
        // 快照时间
        snapshot.put("snapshotTime", System.currentTimeMillis());
        
        log.debug("创建产品信息快照，产品ID: {}, 产品名称: {}", product.getId(), product.getName());
        
        return snapshot;
    }
    
    /**
     * 将产品信息快照Map转换为JSON字符串
     * @param snapshot 产品信息快照Map
     * @return JSON字符串
     */
    public String snapshotToJson(Map<String, Object> snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (Exception e) {
            log.error("产品信息快照转换为JSON失败", e);
            return "{}";
        }
    }
    
    /**
     * 从JSON字符串解析产品信息快照Map
     * @param json JSON字符串
     * @return 产品信息快照Map
     */
    public Map<String, Object> jsonToSnapshot(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.error("JSON字符串解析为产品信息快照失败", e);
            return new HashMap<>();
        }
    }
}