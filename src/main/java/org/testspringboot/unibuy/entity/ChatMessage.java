package org.testspringboot.unibuy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long msgId;

    private Long orderId;
    private Long requestId;
    private Long senderId;
    
    // 1:Student, 2:Merchant, 3:Admin
    private Integer senderRole;
    
    private Long receiverId;
    
    // 1:Student, 2:Merchant, 3:Admin
    private Integer receiverRole;

    private String content;
    
    // 0:Text, 1:Image, 2:System
    private Integer type;
    
    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
