package org.testspringboot.unibuy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("student")
public class Student implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long studentId;

    private String username;
    private String password;
    private String phone;
    private String schoolId;
    private String avatar;
    private String nickname;
    private BigDecimal balance;
    
    // JSON string for addresses
    private String addressList;

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
