package org.testspringboot.unibuy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long productId;

    private Long merchantId;
    private String name;
    private String category;
    private String specs;
    private BigDecimal originalPrice;
    private BigDecimal proxyPrice;
    private Integer stock;
    private String channel;
    private String description;
    private String images; // JSON or comma separated

    // 0:Pending, 1:Pass, 2:Reject
    private Integer auditStatus;

    // 0:Down, 1:Up
    private Integer status;

    private Long sourceRequestId; // 0 if not from request

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
