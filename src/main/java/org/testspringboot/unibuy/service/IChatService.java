package org.testspringboot.unibuy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.testspringboot.unibuy.entity.ChatMessage;
import org.testspringboot.unibuy.entity.vo.ChatSessionVO;
import java.util.List;

public interface IChatService extends IService<ChatMessage> {
    
    /**
     * Send a message
     */
    boolean sendMessage(ChatMessage message);

    /**
     * Get chat history between two users
     * @param userId current user id
     * @param otherId other user id
     * @param role current user role (1: Student, 2: Merchant)
     * @param orderId optional order id
     * @param requestId optional request id
     * @return list of messages
     */
    List<ChatMessage> getChatHistory(Long userId, Long otherId, Integer role, Long orderId, Long requestId);

    /**
     * Get recent contacts for a user
     * @param userId current user id
     * @param role user role (1: Student, 2: Merchant)
     * @return list of user ids
     */
    List<Long> getRecentContacts(Long userId, Integer role);

    /**
     * Send a system notification message
     */
    void sendSystemMessage(Long receiverId, Integer receiverRole, String content, Long orderId);

    /**
     * Get inbox sessions with unread counts
     */
    List<ChatSessionVO> getInboxList(Long userId, Integer role);

    /**
     * Mark messages as read
     */
    void markAsRead(Long userId, Integer role, Long otherId);
}
