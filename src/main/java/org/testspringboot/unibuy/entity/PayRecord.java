package org.testspringboot.unibuy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pay_record")
public class PayRecord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long payId;

    private Long orderId;
    private BigDecimal amount;
    private String method;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
