package org.testspringboot.unibuy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("merchant")
public class Merchant implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long merchantId;

    private String username;
    private String password;
    private String phone;
    private String shopName;
    private String shopAvatar;
    private String shopIntro;
    private String pickupLocation;
    private String qualificationUrl;

    // 0:Pending, 1:Pass, 2:Reject
    private Integer auditStatus;

    // 1:Active, 0:Disabled
    private Integer status;

    private String banReason;
    private LocalDateTime banEndTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
