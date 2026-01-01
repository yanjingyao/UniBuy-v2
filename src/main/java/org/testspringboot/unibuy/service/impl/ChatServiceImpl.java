package org.testspringboot.unibuy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.testspringboot.unibuy.entity.*;
import org.testspringboot.unibuy.entity.vo.ChatSessionVO;
import org.testspringboot.unibuy.mapper.*;
import org.testspringboot.unibuy.service.IChatService;
import org.testspringboot.unibuy.websocket.WebSocketServer;
import java.util.*;

@Service
public class ChatServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatService {

    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private AdminMapper adminMapper;

    @Override
    public boolean sendMessage(ChatMessage message) {
        boolean success = save(message);
        if (success) {
            // Push to receiver via WebSocket
            // Ensure receiverRole is present for correct key generation
            if (message.getReceiverRole() != null) {
                WebSocketServer.sendInfo(message, message.getReceiverRole(), message.getReceiverId());
            }
        }
        return success;
    }

    @Override
    public void sendSystemMessage(Long receiverId, Integer receiverRole, String content, Long orderId) {
        ChatMessage msg = new ChatMessage();
        msg.setSenderId(0L); // System ID
        msg.setSenderRole(0); // System Role
        msg.setReceiverId(receiverId);
        msg.setReceiverRole(receiverRole);
        msg.setContent(content);
        msg.setType(2); // System type
        msg.setOrderId(orderId);
        sendMessage(msg);
    }

    @Override
    public List<ChatMessage> getChatHistory(Long userId, Long otherId, Integer role, Long orderId, Long requestId) {
        QueryWrapper<ChatMessage> query = new QueryWrapper<>();
        
        if (role != null) {
            // (Sender is Me AND Role is MyRole AND Receiver is Other)
            // OR
            // (Receiver is Me AND Role is MyRole AND Sender is Other)
            // Also handle receiver_role=0 for legacy data compatibility
            
            query.and(wrapper -> wrapper
                .nested(i -> i.eq("sender_id", userId).eq("sender_role", role).eq("receiver_id", otherId))
                .or()
                .nested(i -> i.eq("receiver_id", userId).eq("sender_id", otherId)
                        .and(w -> w.eq("receiver_role", role).or().eq("receiver_role", 0)))
            );
        } else {
            // Fallback
            query.and(wrapper -> wrapper
                    .eq("sender_id", userId).eq("receiver_id", otherId)
                    .or()
                    .eq("sender_id", otherId).eq("receiver_id", userId)
            );
        }

        if (orderId != null) query.eq("order_id", orderId);
        if (requestId != null) query.eq("request_id", requestId);

        query.orderByAsc("create_time");
        return list(query);
    }

    @Override
    public List<Long> getRecentContacts(Long userId, Integer role) {
        QueryWrapper<ChatMessage> query = new QueryWrapper<>();
        if (role != null) {
             query.and(w -> w
                 .nested(i -> i.eq("sender_id", userId).eq("sender_role", role))
                 .or()
                 .nested(i -> i.eq("receiver_id", userId)
                         .and(sub -> sub.eq("receiver_role", role).or().eq("receiver_role", 0)))
             );
        } else {
            query.eq("sender_id", userId).or().eq("receiver_id", userId);
        }
        
        query.orderByDesc("create_time");
        query.last("LIMIT 100");

        List<ChatMessage> messages = list(query);
        Set<Long> contactIds = new HashSet<>();
        List<Long> result = new ArrayList<>();

        for (ChatMessage msg : messages) {
            boolean isSender = msg.getSenderId().equals(userId) && (role == null || msg.getSenderRole().equals(role));
            // If I am sender, contact is receiver. If I am receiver, contact is sender.
            Long otherId = isSender ? msg.getReceiverId() : msg.getSenderId();
            
            // Filter out System (0) if needed, but requirements say we receive system msg.
            // But getRecentContacts is usually for Chat list. System msg might be a separate entry "System Notification".
            // Let's keep it for now.
            if (contactIds.add(otherId)) {
                result.add(otherId);
            }
        }
        return result;
    }

    @Override
    public List<ChatSessionVO> getInboxList(Long userId, Integer role) {
        // 1. Fetch all messages related to me
        QueryWrapper<ChatMessage> query = new QueryWrapper<>();
        query.eq("sender_id", userId).or().eq("receiver_id", userId);
        query.orderByDesc("create_time");
        
        List<ChatMessage> allMsgs = list(query);
        Map<String, ChatSessionVO> sessionMap = new LinkedHashMap<>();

        for (ChatMessage msg : allMsgs) {
            // Identify other party
            boolean isSender = msg.getSenderId().equals(userId);
            // If I am sender, other is receiver. If I am receiver, other is sender.
            Long otherId = isSender ? msg.getReceiverId() : msg.getSenderId();
            Integer otherRole = isSender ? msg.getReceiverRole() : msg.getSenderRole();
            
            // Key for session: role_id (e.g. 1_101)
            // If role is missing (old data), fallback logic needed, but let's assume valid.
            if (otherRole == null) otherRole = 0;
            
            String key = otherRole + "_" + otherId;
            
            // Handle System Messages (Role 0)
            if (msg.getSenderRole() == 0) {
                key = "0_0";
                otherId = 0L;
                otherRole = 0;
            }

            ChatSessionVO vo = sessionMap.get(key);
            if (vo == null) {
                vo = new ChatSessionVO();
                vo.setOtherId(otherId);
                vo.setOtherRole(otherRole);
                vo.setLastMessage(msg.getType() == 1 ? "[图片]" : msg.getContent());
                vo.setLastTime(msg.getCreateTime());
                vo.setUnreadCount(0);
                vo.setIsSystem(otherRole == 0);
                
                // Fetch Name/Avatar
                if (otherRole == 0) {
                    vo.setOtherName("系统通知");
                    vo.setOtherAvatar(""); // Default system icon on frontend
                } else if (otherRole == 1) {
                    Student s = studentMapper.selectById(otherId);
                    if (s != null) {
                        vo.setOtherName(s.getNickname() != null ? s.getNickname() : s.getUsername());
                        vo.setOtherAvatar(s.getAvatar());
                    } else {
                        vo.setOtherName("未知学生");
                    }
                } else if (otherRole == 2) {
                    Merchant m = merchantMapper.selectById(otherId);
                    if (m != null) {
                        vo.setOtherName(m.getShopName());
                        vo.setOtherAvatar(m.getShopAvatar());
                    } else {
                        vo.setOtherName("未知商家");
                    }
                } else if (otherRole == 3) {
                    Admin a = adminMapper.selectById(otherId);
                    if (a != null) {
                        // Use realName if available, else username, else "管理员"
                        String name = a.getRealName();
                        if (name == null || name.isEmpty()) name = a.getUsername();
                        if (name == null || name.isEmpty()) name = "管理员";
                        vo.setOtherName(name);
                        // vo.setOtherAvatar(a.getAvatar()); // Admin usually has no avatar field
                    } else {
                        vo.setOtherName("管理员");
                    }
                }
                
                sessionMap.put(key, vo);
            }
            
            // Count unread: Message is sent TO ME, and isRead=0
            if (!isSender && msg.getIsRead() == 0) {
                vo.setUnreadCount(vo.getUnreadCount() + 1);
            }
        }
        
        return new ArrayList<>(sessionMap.values());
    }

    @Override
    public void markAsRead(Long userId, Integer role, Long otherId) {
        // Update chat_message set is_read=1 where receiver_id=userId and sender_id=otherId
        
        // Handling System Messages
        if (otherId == 0) {
            // Mark all system messages as read
            ChatMessage update = new ChatMessage();
            update.setIsRead(1);
            
            QueryWrapper<ChatMessage> updateWrapper = new QueryWrapper<>();
            updateWrapper.eq("receiver_id", userId).eq("sender_role", 0).eq("is_read", 0);
            update(update, updateWrapper);
        } else {
            ChatMessage update = new ChatMessage();
            update.setIsRead(1);
            
            QueryWrapper<ChatMessage> updateWrapper = new QueryWrapper<>();
            updateWrapper.eq("receiver_id", userId).eq("sender_id", otherId).eq("is_read", 0);
            update(update, updateWrapper);
        }
    }
}
