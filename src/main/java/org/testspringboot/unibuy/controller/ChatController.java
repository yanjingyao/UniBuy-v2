package org.testspringboot.unibuy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.testspringboot.unibuy.common.Result;
import org.testspringboot.unibuy.entity.ChatMessage;
import org.testspringboot.unibuy.entity.vo.ChatSessionVO;
import org.testspringboot.unibuy.service.IChatService;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private IChatService chatService;

    @GetMapping("/inbox")
    public Result<List<ChatSessionVO>> getInbox(@RequestParam Long userId, @RequestParam Integer role) {
        return Result.success(chatService.getInboxList(userId, role));
    }

    @PostMapping("/read")
    public Result<?> markRead(@RequestParam Long userId, @RequestParam Integer role, @RequestParam Long otherId) {
        chatService.markAsRead(userId, role, otherId);
        return Result.success();
    }

    @PostMapping("/send")
    public Result<?> sendMessage(@RequestBody ChatMessage message) {
        if (message.getSenderId() == null || message.getReceiverId() == null) {
            return Result.error("Sender and Receiver are required");
        }
        boolean success = chatService.sendMessage(message);
        return success ? Result.success() : Result.error("Send failed");
    }

    @GetMapping("/history")
    public Result<List<ChatMessage>> getHistory(
            @RequestParam Long userId,
            @RequestParam Long otherId,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long requestId) {
        
        List<ChatMessage> list = chatService.getChatHistory(userId, otherId, role, orderId, requestId);
        return Result.success(list);
    }
    
    @GetMapping("/contacts")
    public Result<List<Long>> getContacts(@RequestParam Long userId, @RequestParam Integer role) {
        List<Long> contacts = chatService.getRecentContacts(userId, role);
        return Result.success(contacts);
    }
}
