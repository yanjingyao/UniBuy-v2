package org.testspringboot.unibuy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.testspringboot.unibuy.common.Result;
import org.testspringboot.unibuy.entity.ChatMessage;
import org.testspringboot.unibuy.entity.Reviews;
import org.testspringboot.unibuy.mapper.ChatMessageMapper;
import org.testspringboot.unibuy.mapper.ReviewsMapper;

import java.util.List;

@RestController
@RequestMapping("/aux")
public class AuxController {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ReviewsMapper reviewsMapper;

    @PostMapping("/chat/send")
    public Result<String> sendChat(@RequestBody ChatMessage message) {
        message.setIsRead(0);
        chatMessageMapper.insert(message);
        return Result.success("Message sent");
    }

    @GetMapping("/chat/history")
    public Result<List<ChatMessage>> chatHistory(@RequestParam(required = false) Long orderId,
                                                 @RequestParam(required = false) Long requestId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        if (orderId != null) wrapper.eq(ChatMessage::getOrderId, orderId);
        if (requestId != null) wrapper.eq(ChatMessage::getRequestId, requestId);
        wrapper.orderByAsc(ChatMessage::getCreateTime);
        return Result.success(chatMessageMapper.selectList(wrapper));
    }

    @PostMapping("/review/submit")
    public Result<String> submitReview(@RequestBody Reviews review) {
        reviewsMapper.insert(review);
        return Result.success("Review submitted");
    }

    @GetMapping("/review/list")
    public Result<List<Reviews>> listReviews(@RequestParam Long merchantId) {
        return Result.success(reviewsMapper.selectList(
                new LambdaQueryWrapper<Reviews>().eq(Reviews::getMerchantId, merchantId)
        ));
    }
}
