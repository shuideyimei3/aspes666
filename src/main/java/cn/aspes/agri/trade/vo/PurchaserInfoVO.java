package cn.aspes.agri.trade.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "采购商信息VO")
public class PurchaserInfoVO {

    @Schema(description = "采购商ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "企业名称")
    private String companyName;

    @Schema(description = "统一社会信用代码")
    private String creditCode;

    @Schema(description = "法人代表")
    private String legalPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "所在地区编码")
    private String areaCode;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "营业执照")
    private String businessLicense;

    @Schema(description = "认证状态 0-待审核 1-已认证 2-审核拒绝")
    private Integer authStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}