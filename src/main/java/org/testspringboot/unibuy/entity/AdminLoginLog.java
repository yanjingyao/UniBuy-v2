package org.testspringboot.unibuy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_login_log")
public class AdminLoginLog {
    @TableId(type = IdType.AUTO)
    private Long logId;
    
    private Long adminId;         // 管理员ID
    private String adminUsername; // 管理员用户名
    private String loginIp;       // 登录IP地址
    private String userAgent;     // 用户代理信息
    private String loginStatus;   // 登录状态: SUCCESS/FAILED
    private String failureReason; // 失败原因（如果登录失败）
    private LocalDateTime loginTime; // 登录时间
    private String location;      // 登录地点（可选）
}