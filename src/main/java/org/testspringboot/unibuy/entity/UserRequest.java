package org.testspringboot.unibuy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_request")
public class UserRequest implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long requestId;

    private Long studentId;
    private String productName;
    private String category;
    private String description;
    private String address;
    private BigDecimal expectedPrice;
    private BigDecimal budgetFee; // Proxy fee budget
    private String purchaseLocation; // Specific location
    private String refImage;
    private LocalDateTime deadline;

    // 1:Low, 2:Medium, 3:High
    private Integer urgencyLevel;

    private Integer minJoinUsers;

    // 0:Pending, 1:Responding, 2:Accepted, 3:Expired, 4:Completed
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
