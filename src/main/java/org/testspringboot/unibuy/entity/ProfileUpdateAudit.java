package org.testspringboot.unibuy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("profile_update_audit")
public class ProfileUpdateAudit {
    @TableId(type = IdType.AUTO)
    private Long auditId;
    
    private Long userId;          // 用户ID
    private String role;          // 用户角色: student/merchant
    private String fieldName;     // 修改的字段名
    private String oldValue;      // 原值
    private String newValue;      // 新值
    private String reason;        // 修改原因
    private String evidenceUrls;  // 证明材料URLs，JSON格式
    private Integer status;       // 审核状态: 0-待审核, 1-通过, 2-拒绝
    private String auditNote;     // 审核备注
    private Long auditorId;       // 审核人ID（管理员）
    private LocalDateTime auditTime; // 审核时间
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;      // 逻辑删除标记
}