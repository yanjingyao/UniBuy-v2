package org.testspringboot.unibuy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("admin")
public class Admin implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long adminId;

    private String username;
    private String password;
    private String realName;
    private String phone;
    
    // 0:Super, 1:Normal
    private Integer roleLevel;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
