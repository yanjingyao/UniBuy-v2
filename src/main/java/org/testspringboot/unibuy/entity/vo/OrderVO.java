package org.testspringboot.unibuy.entity.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.testspringboot.unibuy.entity.Orders;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderVO extends Orders {
    private String studentName;
    private String merchantName;
}
