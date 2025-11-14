package cn.aspes.agri.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 产地信息实体类
 */
@Data
@TableName("origin_area")
public class OriginArea {
    
    @TableId(type = IdType.AUTO)
    private Integer areaId;
    
    private String areaCode;
    
    private String areaName;
    
    private String province;
    
    private String city;
    
    private String feature;
    
    private Boolean isPovertyArea;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
