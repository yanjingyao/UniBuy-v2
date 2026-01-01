package org.testspringboot.unibuy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("login_log")
public class LoginLog {
    @TableId(type = IdType.AUTO)
    private Long logId;
    
    private Long userId;
    private String username;
    private String role; // student, merchant, admin
    
    private String ip;
    private String userAgent;
    
    private String status; // SUCCESS, FAIL
    private String msg;    // Failure reason
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
