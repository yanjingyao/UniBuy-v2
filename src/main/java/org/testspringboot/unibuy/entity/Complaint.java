package org.testspringboot.unibuy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("complaint")
public class Complaint {
    @TableId(type = IdType.AUTO)
    private Long complaintId;
    
    private Long complainantId;    // 投诉人ID
    private String complainantRole; // 投诉人角色: student/merchant
    private Long respondentId;     // 被投诉人ID
    private String respondentRole; // 被投诉人角色: student/merchant/admin
    private String complaintType;  // 投诉类型
    private String complaintContent; // 投诉内容
    private String evidenceUrls;   // 证据图片URLs，JSON格式
    private Integer status;        // 状态: 0-待处理, 1-已解决, 2-已拒绝
    private String resultNote;     // 处理结果说明
    private Long processorId;      // 处理人ID（管理员）
    private LocalDateTime processTime; // 处理时间
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;       // 逻辑删除标记
}