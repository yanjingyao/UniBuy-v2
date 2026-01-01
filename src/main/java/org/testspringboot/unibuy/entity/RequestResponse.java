package org.testspringboot.unibuy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("request_response")
public class RequestResponse implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long responseId;

    private Long requestId;
    private Long merchantId;
    private BigDecimal quotedPrice;
    private BigDecimal serviceFee; // Service fee
    private String responseNote;
    
    // 0:No, 1:Yes
    private Integer isSelected;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
