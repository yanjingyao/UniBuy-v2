package org.testspringboot.unibuy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.testspringboot.unibuy.common.Result;
import org.testspringboot.unibuy.entity.PayRecord;
import org.testspringboot.unibuy.service.IPayService;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Autowired
    private IPayService payService;

    @PostMapping("/confirm")
    public Result<String> confirm(@RequestParam Long orderId, @RequestParam String method) {
        payService.processPayment(orderId, method);
        return Result.success("Payment successful");
    }

    @GetMapping("/history")
    public Result<Page<PayRecord>> history(@RequestParam Long userId,
                                           @RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<PayRecord> page = new Page<>(pageNum, pageSize);
        return Result.success(payService.listHistory(page, userId));
    }
}
