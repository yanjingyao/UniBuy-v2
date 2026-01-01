package org.testspringboot.unibuy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.testspringboot.unibuy.common.Result;
import org.testspringboot.unibuy.entity.RechargeRecord;
import org.testspringboot.unibuy.service.IStudentService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private IStudentService studentService;

    @GetMapping("/balance")
    public Result<BigDecimal> getBalance(@RequestParam Long studentId) {
        return Result.success(studentService.getBalance(studentId));
    }

    @PostMapping("/recharge")
    public Result<String> recharge(@RequestBody Map<String, Object> params) {
        Long studentId = Long.valueOf(params.get("studentId").toString());
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String method = params.get("method").toString();
        
        studentService.recharge(studentId, amount, method);
        return Result.success("Recharge success");
    }

    @GetMapping("/recharge/history")
    public Result<List<RechargeRecord>> getHistory(@RequestParam Long studentId) {
        return Result.success(studentService.getRechargeHistory(studentId));
    }
}
