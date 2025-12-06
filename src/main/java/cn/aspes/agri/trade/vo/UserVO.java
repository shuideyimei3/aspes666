package cn.aspes.agri.trade.vo;

import cn.aspes.agri.trade.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户视图对象
 */
@Data
public class UserVO {
    
    private Long id;
    
    private String username;
    
    private UserRole role;
    
    private String contactPerson;
    
    private String contactPhone;
    
    private String contactEmail;
    
    private Integer isCertified;
    
    private LocalDateTime createTime;
}
