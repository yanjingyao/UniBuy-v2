package org.testspringboot.unibuy.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatSessionVO {
    private Long otherId;
    private String otherName; // Nickname or Shop Name
    private String otherAvatar;
    private Integer otherRole;
    
    private String lastMessage;
    private LocalDateTime lastTime;
    private Integer unreadCount;
    
    // For system notification
    private Boolean isSystem;
}
