package org.testspringboot.unibuy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("recharge_record")
public class RechargeRecord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long rechargeId;

    private Long studentId;
    private BigDecimal amount;
    private String method; // WeChat, Alipay, Bank
    private Integer status; // 1:Success

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
