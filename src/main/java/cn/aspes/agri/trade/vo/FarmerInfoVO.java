package cn.aspes.agri.trade.vo;

import cn.aspes.agri.trade.enums.AuditStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 农户信息VO类
 */
@Data
@Schema(description = "农户信息VO")
public class FarmerInfoVO {

    @Schema(description = "农户ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "农场名称")
    private String farmName;

    @Schema(description = "产地ID")
    private Integer originAreaId;

    @Schema(description = "产地名称")
    private String originAreaName;

    @Schema(description = "生产规模")
    private String productionScale;

    @Schema(description = "认证信息")
    private Map<String, String> certifications;

    @Schema(description = "银行账户")
    private String bankAccount;

    @Schema(description = "开户银行")
    private String bankName;

    @Schema(description = "身份证号")
    private String idNumber;

    @Schema(description = "身份证正面照片URL")
    private String idCardFrontUrl;

    @Schema(description = "身份证反面照片URL")
    private String idCardBackUrl;

    @Schema(description = "申请理由")
    private String applyReason;

    @Schema(description = "认证状态")
    private AuditStatus auditStatus;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "申请时间")
    private LocalDateTime createTime;

    @Schema(description = "审核通过时间")
    private LocalDateTime approvedTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}