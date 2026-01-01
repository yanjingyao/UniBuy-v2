package org.testspringboot.unibuy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Orders implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long orderId;

    private String orderNo;
    private Long studentId;
    private Long merchantId;
    private BigDecimal totalPrice;
    private BigDecimal serviceFee; // Proxy service fee included
    
    // 0:Unpaid, 1:Paid
    private Integer payStatus;
    
    // 0:WaitPay, 1:WaitAccept, 2:WaitPickup, 3:Done, 4:Cancelled
    private Integer orderStatus;
    
    private String addressSnapshot;
    private Long sourceRequestId;
    private String voucherImg;
    private String cancelReason;

    private LocalDateTime payTime;
    private LocalDateTime acceptTime;
    private LocalDateTime finishTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
