package cn.aspes.agri.trade.entity;

import cn.aspes.agri.trade.enums.UserRole;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("user")
public class User {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private String username;
    
    private String password;
    
    private UserRole role;
    
    private String contactPerson;
    
    private String contactPhone;
    
    private String contactEmail;
    
    private Integer isCertified;
    
    @TableLogic
    private Integer isDelete;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}