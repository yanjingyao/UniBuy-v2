package org.testspringboot.unibuy.controller;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.testspringboot.unibuy.common.Result;
import org.testspringboot.unibuy.entity.Admin;
import org.testspringboot.unibuy.entity.Merchant;
import org.testspringboot.unibuy.entity.Student;
import org.testspringboot.unibuy.service.IAdminService;
import org.testspringboot.unibuy.service.IMerchantService;
import org.testspringboot.unibuy.service.IStudentService;
import org.testspringboot.unibuy.service.ILoginLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.testspringboot.unibuy.entity.AdminLoginLog;
import org.testspringboot.unibuy.mapper.AdminLoginLogMapper;
import org.testspringboot.unibuy.mapper.SysConfigMapper;
import org.testspringboot.unibuy.entity.SysConfig;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private IStudentService studentService;

    @Autowired
    private IMerchantService merchantService;

    @Autowired
    private IAdminService adminService;
    
    @Autowired
    private ILoginLogService loginLogService;
    
    @Autowired
    private AdminLoginLogMapper adminLoginLogMapper;
    
    @Autowired
    private SysConfigMapper sysConfigMapper;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        Map<String, Object> data = new HashMap<>();
        String role = request.getRole();
        String username = request.getUsername();
        String ip = servletRequest.getRemoteAddr();
        String ua = servletRequest.getHeader("User-Agent");
        
        if ("student".equalsIgnoreCase(role)) {
            Student student = studentService.login(request.getUsername(), request.getPassword());
            if (student == null) {
                loginLogService.log(null, username, "student", ip, ua, false, "用户名或密码错误");
                return Result.error("登录失败，请检查用户名或密码");
            }
            if (student.getStatus() != null && student.getStatus() == 0) {
                String reason = getBanMessage(student.getBanReason(), student.getBanEndTime());
                loginLogService.log(student.getStudentId(), username, "student", ip, ua, false, "账号已禁用: " + reason);
                return Result.error(reason);
            }
            loginLogService.log(student.getStudentId(), student.getUsername(), "student", ip, ua, true, "登录成功");
            data.put("user", student);
            data.put("role", "student");
        } else if ("merchant".equalsIgnoreCase(role)) {
            try {
                Merchant merchant = merchantService.login(request.getUsername(), request.getPassword());
                if (merchant == null) {
                    loginLogService.log(null, username, "merchant", ip, ua, false, "用户名或密码错误");
                    return Result.error("登录失败，请检查用户名或密码");
                }
                if (merchant.getStatus() != null && merchant.getStatus() == 0) {
                    String reason = getBanMessage(merchant.getBanReason(), merchant.getBanEndTime());
                    loginLogService.log(merchant.getMerchantId(), username, "merchant", ip, ua, false, "账号已禁用: " + reason);
                    return Result.error(reason);
                }
                loginLogService.log(merchant.getMerchantId(), merchant.getUsername(), "merchant", ip, ua, true, "登录成功");
                data.put("user", merchant);
                data.put("role", "merchant");
            } catch (RuntimeException e) {
                loginLogService.log(null, username, "merchant", ip, ua, false, "异常: " + e.getMessage());
                return Result.error(e.getMessage());
            }
        } else if ("admin".equalsIgnoreCase(role)) {
            Admin admin = adminService.login(request.getUsername(), request.getPassword());
            if (admin == null) {
                // Log failed admin login
                AdminLoginLog log = new AdminLoginLog();
                log.setAdminId(null);
                log.setAdminUsername(username);
                log.setLoginIp(ip);
                log.setUserAgent(ua);
                log.setLoginStatus("FAILED");
                log.setFailureReason("用户名或密码错误");
                log.setLoginTime(java.time.LocalDateTime.now());
                adminLoginLogMapper.insert(log);
                
                return Result.error("Invalid username or password");
            }
            
            // Log successful admin login
            AdminLoginLog log = new AdminLoginLog();
            log.setAdminId(admin.getAdminId());
            log.setAdminUsername(admin.getUsername());
            log.setLoginIp(ip);
            log.setUserAgent(ua);
            log.setLoginStatus("SUCCESS");
            log.setLoginTime(java.time.LocalDateTime.now());
            adminLoginLogMapper.insert(log);
            
            data.put("user", admin);
            data.put("role", "admin");
        } else {
            return Result.error("Invalid role");
        }
        
        return Result.success(data);
    }
    
    @GetMapping("/system/phone")
    public Result<String> getSystemPhone() {
        SysConfig config = sysConfigMapper.selectById("system_phone");
        return Result.success(config != null ? config.getConfigValue() : "");
    }

    @PostMapping("/register/student")
    public Result<String> registerStudent(@RequestBody Student student) {
        studentService.register(student);
        return Result.success("Register success");
    }

    @PostMapping("/register/merchant")
    public Result<String> registerMerchant(@RequestBody Merchant merchant) {
        merchantService.register(merchant);
        return Result.success("Register success, please wait for audit");
    }

    @PostMapping("/code")
    public Result<String> sendCode(@RequestParam String phone) {
        // Mock sending code
        System.out.println("Sending code 123456 to " + phone);
        return Result.success("123456");
    }

    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        // Mock verify code
        if (!"123456".equals(request.getCode())) {
            return Result.error("Invalid verification code");
        }

        if ("student".equalsIgnoreCase(request.getRole())) {
            studentService.resetPassword(request.getPhone(), request.getNewPassword());
        } else if ("merchant".equalsIgnoreCase(request.getRole())) {
            merchantService.resetPassword(request.getPhone(), request.getNewPassword());
        } else {
            return Result.error("Invalid role");
        }
        return Result.success("Password reset success");
    }

    private String getBanMessage(String reason, LocalDateTime endTime) {
        String r = reason == null ? "无" : reason;
        String timeStr;
        if (endTime == null) {
            timeStr = "永久";
        } else {
            if (endTime.isBefore(LocalDateTime.now())) {
                timeStr = "已过期";
            } else {
                java.time.Duration duration = java.time.Duration.between(LocalDateTime.now(), endTime);
                long days = duration.toDays();
                long hours = duration.toHours() % 24;
                long minutes = duration.toMinutes() % 60;
                timeStr = days + "天" + hours + "小时" + minutes + "分";
            }
        }
        return "账号已被禁用。原因: " + r + "。剩余时间: " + timeStr;
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
        private String role; // student, merchant, admin
    }

    @Data
    public static class ResetPasswordRequest {
        private String phone;
        private String code;
        private String newPassword;
        private String role;
    }
}
