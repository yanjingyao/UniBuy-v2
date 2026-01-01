package org.testspringboot.unibuy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("order_item")
public class OrderItem implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long itemId;

    private Long orderId;
    private Long productId;
    private String productName;
    private String productImg;
    private Integer quantity;
    private BigDecimal price;
}
