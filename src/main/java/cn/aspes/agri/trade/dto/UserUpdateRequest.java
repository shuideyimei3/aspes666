package cn.aspes.agri.trade.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户信息修改请求
 */
@Data
public class UserUpdateRequest {
    
    @NotBlank(message = "联系人姓名不能为空")
    private String contactPerson;
    
    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;
    
    @Email(message = "邮箱格式不正确")
    private String contactEmail;
}
