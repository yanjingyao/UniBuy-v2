package org.testspringboot.unibuy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.testspringboot.unibuy.common.Result;
import org.testspringboot.unibuy.entity.*;
import org.testspringboot.unibuy.service.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IStudentService studentService;

    @Autowired
    private IMerchantService merchantService;
    
    @Autowired
    private IOrderService orderService;
    
    @Autowired
    private IProductService productService;

    @Autowired
    private IRequestService requestService;

    @Autowired
    private IAdminService adminService;

    // --- Student Profile ---
    @PostMapping("/student/update")
    public Result<?> updateStudentProfile(@RequestBody Student student) {
        if (student.getStudentId() == null) return Result.error("Student ID required");
        Student exist = studentService.getById(student.getStudentId());
        if (exist == null) return Result.error("User not found");
        
        // Update fields
        if (student.getNickname() != null) exist.setNickname(student.getNickname());
        if (student.getAvatar() != null) exist.setAvatar(student.getAvatar());
        if (student.getAddressList() != null) exist.setAddressList(student.getAddressList());
        
        studentService.updateById(exist);
        return Result.success(exist);
    }



    // --- Merchant Profile ---
    @PostMapping("/merchant/update")
    public Result<?> updateMerchantProfile(@RequestBody Merchant merchant) {
        if (merchant.getMerchantId() == null) return Result.error("Merchant ID required");
        Merchant exist = merchantService.getById(merchant.getMerchantId());
        if (exist == null) return Result.error("User not found");

        // Update fields
        if (merchant.getShopName() != null) exist.setShopName(merchant.getShopName());
        if (merchant.getShopAvatar() != null) exist.setShopAvatar(merchant.getShopAvatar());
        if (merchant.getShopIntro() != null) exist.setShopIntro(merchant.getShopIntro());
        if (merchant.getPickupLocation() != null) exist.setPickupLocation(merchant.getPickupLocation());
        if (merchant.getPhone() != null) exist.setPhone(merchant.getPhone());
        
        merchantService.updateById(exist);
        return Result.success(exist);
    }
    
    // --- Common Security ---
    @PostMapping("/password/change")
    public Result<?> changePassword(@RequestBody ChangePasswordRequest req) {
        if ("student".equals(req.getRole())) {
            Student s = studentService.getById(req.getUserId());
            if (s == null) return Result.error("User not found");
            if (!s.getPassword().equals(req.getOldPassword())) return Result.error("Old password incorrect");
            s.setPassword(req.getNewPassword());
            studentService.updateById(s);
        } else if ("merchant".equals(req.getRole())) {
            Merchant m = merchantService.getById(req.getUserId());
            if (m == null) return Result.error("User not found");
            if (!m.getPassword().equals(req.getOldPassword())) return Result.error("Old password incorrect");
            m.setPassword(req.getNewPassword());
            merchantService.updateById(m);
        } else {
            return Result.error("Invalid role");
        }
        return Result.success("Password changed");
    }
    
    @PostMapping("/phone/rebind")
    public Result<?> rebindPhone(@RequestBody RebindPhoneRequest req) {
        if (!"123456".equals(req.getCode())) return Result.error("Invalid verification code");
        
        if ("student".equals(req.getRole())) {
            Student s = studentService.getById(req.getUserId());
            s.setPhone(req.getNewPhone());
            studentService.updateById(s);
        } else if ("merchant".equals(req.getRole())) {
            Merchant m = merchantService.getById(req.getUserId());
            m.setPhone(req.getNewPhone());
            merchantService.updateById(m);
        } else {
            return Result.error("Invalid role");
        }
        return Result.success("Phone updated");
    }
    
    // --- Statistics ---
    @GetMapping("/merchant/stats")
    public Result<Map<String, Object>> getMerchantStats(@RequestParam Long merchantId) {
        Map<String, Object> stats = new HashMap<>();
        
        // Monthly Sales (Simple: count completed orders)
        QueryWrapper<Orders> qOrders = new QueryWrapper<>();
        qOrders.eq("merchant_id", merchantId).eq("order_status", 3); // Completed
        // Today's Sales
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        qOrders.ge("finish_time", startOfDay);
        
        // Sum total price manually or via custom SQL. 
        // For simple MP wrapper, we fetch list. If list is huge, this is bad, but for MVP:
        java.util.List<Orders> list = orderService.list(qOrders);
        java.math.BigDecimal todaySales = list.stream()
            .map(Orders::getTotalPrice)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        stats.put("todaySales", todaySales);
        
        // Pending Orders
        QueryWrapper<Orders> qPending = new QueryWrapper<>();
        qPending.eq("merchant_id", merchantId).in("order_status", 1, 2); // To accept or To deliver
        stats.put("pendingOrders", orderService.count(qPending));
        
        // Pending Requests (Assume merchant wants to see new requests in their category? Or just total available)
        // For now return total available in pool
        QueryWrapper<UserRequest> qReq = new QueryWrapper<>();
        qReq.eq("status", 0); // Pending
        stats.put("pendingRequests", requestService.count(qReq));
        
        // Active Products
        QueryWrapper<Product> qProd = new QueryWrapper<>();
        qProd.eq("merchant_id", merchantId).eq("status", 1); // Up
        stats.put("activeProducts", productService.count(qProd));
        
        // Total Sales (All Time)
        // Note: For now we sum totalPrice of all completed orders.
        // If you want to include Service Fee as separate, or handle partial refunds, adjust here.
        // Assuming totalPrice in Orders table ALREADY includes service_fee.
        QueryWrapper<Orders> qTotal = new QueryWrapper<>();
        qTotal.eq("merchant_id", merchantId).eq("order_status", 3); // Completed
        java.util.List<Orders> totalList = orderService.list(qTotal);
        java.math.BigDecimal totalSales = totalList.stream()
            .map(Orders::getTotalPrice)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        stats.put("totalSales", totalSales);
        
        return Result.success(stats);
    }
    
    @GetMapping("/student/stats")
    public Result<Map<String, Object>> getStudentStats(@RequestParam Long studentId) {
        Map<String, Object> stats = new HashMap<>();
        
        // Orders
        QueryWrapper<Orders> qOrd = new QueryWrapper<>();
        qOrd.eq("student_id", studentId);
        stats.put("orders", orderService.count(qOrd));
        
        // Requests
        QueryWrapper<UserRequest> qReq = new QueryWrapper<>();
        qReq.eq("student_id", studentId);
        stats.put("requests", requestService.count(qReq));
        
        return Result.success(stats);
    }
    
    @GetMapping("/merchant/public-info")
    public Result<Merchant> getMerchantPublicInfo(@RequestParam Long merchantId) {
        Merchant m = merchantService.getById(merchantId);
        if (m == null) return Result.error("Merchant not found");
        // Hide password
        m.setPassword(null);
        return Result.success(m);
    }

    @GetMapping("/public-info")
    public Result<Map<String, Object>> getPublicInfo(@RequestParam Long userId, @RequestParam Integer role) {
        Map<String, Object> info = new HashMap<>();
        if (role == 1) { // Student
            Student s = studentService.getById(userId);
            if (s != null) {
                info.put("name", s.getNickname() != null ? s.getNickname() : s.getUsername());
                info.put("nickname", s.getNickname());
                info.put("username", s.getUsername());
                info.put("avatar", s.getAvatar());
                return Result.success(info);
            }
        } else if (role == 2) { // Merchant
             Merchant m = merchantService.getById(userId);
             if (m != null) {
                 info.put("name", m.getShopName());
                 info.put("shopName", m.getShopName());
                 info.put("avatar", m.getShopAvatar());
                 return Result.success(info);
             }
        } else if (role == 3) { // Admin
            Admin a = adminService.getById(userId);
            if (a != null) {
                String name = a.getRealName();
                if (name == null || name.isEmpty()) name = a.getUsername();
                info.put("name", name);
                info.put("realName", a.getRealName());
                info.put("username", a.getUsername());
                // Admin might not have avatar, frontend can use default
                info.put("avatar", ""); 
                return Result.success(info);
            }
        }
        return Result.error("User not found");
    }

    @GetMapping("/support/list")
    public Result<java.util.List<Map<String, Object>>> getSupportList() {
        // Return all admins for support
        // You might want to filter only certain roles or active admins
        java.util.List<Admin> admins = adminService.list();
        java.util.List<Map<String, Object>> result = new java.util.ArrayList<>();
        
        for (Admin a : admins) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getAdminId());
            map.put("username", a.getUsername());
            map.put("realName", a.getRealName());
            map.put("role", 3); // Admin role code
            // Only return necessary info
            result.add(map);
        }
        return Result.success(result);
    }

    public static class ChangePasswordRequest {
        private Long userId;
        private String role;
        private String oldPassword;
        private String newPassword;
        // getters setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
    
    public static class RebindPhoneRequest {
        private Long userId;
        private String role;
        private String newPhone;
        private String code;
        // getters setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getNewPhone() { return newPhone; }
        public void setNewPhone(String newPhone) { this.newPhone = newPhone; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
}
