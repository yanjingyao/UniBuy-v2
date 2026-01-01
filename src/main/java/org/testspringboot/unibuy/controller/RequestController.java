package org.testspringboot.unibuy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.testspringboot.unibuy.common.Result;
import org.testspringboot.unibuy.entity.RequestResponse;
import org.testspringboot.unibuy.entity.UserRequest;
import org.testspringboot.unibuy.service.IRequestService;

import java.util.List;

@RestController
@RequestMapping("/request")
public class RequestController {

    @Autowired
    private IRequestService requestService;

    @PostMapping("/publish")
    public Result<String> publish(@RequestBody UserRequest request) {
        requestService.publishRequest(request);
        return Result.success("Request published");
    }

    @GetMapping("/square")
    public Result<Page<UserRequest>> square(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize,
                                            @RequestParam(required = false) String category) {
        Page<UserRequest> page = new Page<>(pageNum, pageSize);
        return Result.success(requestService.listSquare(page, category));
    }

    @PostMapping("/respond")
    public Result<String> respond(@RequestBody RequestResponse response) {
        requestService.respondRequest(response);
        return Result.success("Response submitted");
    }

    @GetMapping("/responses")
    public Result<List<RequestResponse>> getResponses(@RequestParam Long requestId) {
        return Result.success(requestService.listResponses(requestId));
    }

    @PostMapping("/confirm")
    public Result<Long> confirm(@RequestParam Long requestId, @RequestParam Long responseId) {
        Long orderId = requestService.confirmSelection(requestId, responseId);
        return Result.success(orderId);
    }

    @GetMapping("/my")
    public Result<List<UserRequest>> myRequests(@RequestParam Long studentId) {
        return Result.success(requestService.listByStudent(studentId));
    }
}
