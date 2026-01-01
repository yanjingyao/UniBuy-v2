package org.testspringboot.unibuy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;
import org.testspringboot.unibuy.common.Result;
import org.testspringboot.unibuy.entity.*;
import org.testspringboot.unibuy.mapper.*;
import org.testspringboot.unibuy.service.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private IMerchantService merchantService;
    
    @Autowired
    private IStudentService studentService;
    
    @Autowired
    private IProductService productService;

    @Autowired
    private IOrderService orderService;
    
    @Autowired
    private IAdminService adminService;
    
    @Autowired
    private AnnouncementMapper announcementMapper;
    
    @Autowired
    private SysConfigMapper sysConfigMapper;

    @Autowired
    private ComplaintMapper complaintMapper;

    @Autowired
    private ProfileUpdateAuditMapper profileUpdateAuditMapper;

    @Autowired
    private AdminLoginLogMapper adminLoginLogMapper;

    

    // --- Admin Management (Super Admin Only) ---
    @PostMapping("/create")
    public Result<?> createAdmin(@RequestBody Admin admin, @RequestParam Long operatorId) {
        if (!isSuperAdmin(operatorId)) return Result.error("Permission denied");
        
        // Check username exist
        Admin exist = adminService.getOne(new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, admin.getUsername()));
        if (exist != null) return Result.error("Username already exists");
        
        admin.setRoleLevel(1); // Force Normal Admin
        adminService.save(admin);
        return Result.success("Admin created");
    }

    @GetMapping("/list")
    public Result<List<Admin>> listAdmins(@RequestParam Long operatorId) {
        if (!isSuperAdmin(operatorId)) return Result.error("Permission denied");
        // Hide passwords
        List<Admin> list = adminService.list();
        list.forEach(a -> a.setPassword(null));
        return Result.success(list);
    }

    @PostMapping("/delete")
    public Result<?> deleteAdmin(@RequestParam Long targetId, @RequestParam Long operatorId) {
        if (!isSuperAdmin(operatorId)) return Result.error("Permission denied");
        
        Admin target = adminService.getById(targetId);
        if (target == null) return Result.error("Target not found");
        if (target.getRoleLevel() == 0) return Result.error("Cannot delete Super Admin");
        
        adminService.removeById(targetId);
        return Result.success("Admin deleted");
    }

    private boolean isSuperAdmin(Long adminId) {
        Admin a = adminService.getById(adminId);
        return a != null && a.getRoleLevel() == 0;
    }

    // --- Admin Profile ---
    @GetMapping("/profile")
    public Result<Admin> getProfile(@RequestParam Long adminId) {
        Admin admin = adminService.getById(adminId);
        if (admin == null) return Result.error("Admin not found");
        admin.setPassword(null); // hide password
        return Result.success(admin);
    }

    @PostMapping("/profile/update")
    public Result<Admin> updateProfile(@RequestBody Admin admin) {
        if (admin.getAdminId() == null) return Result.error("Admin ID required");
        Admin exist = adminService.getById(admin.getAdminId());
        if (exist == null) return Result.error("Admin not found");
        
        if (admin.getRealName() != null) exist.setRealName(admin.getRealName());
        if (admin.getPhone() != null) exist.setPhone(admin.getPhone());
        
        adminService.updateById(exist);
        exist.setPassword(null);
        return Result.success(exist);
    }

    @PostMapping("/password/change")
    public Result<?> changePassword(@RequestBody Map<String, Object> params) {
        Long adminId = Long.valueOf(params.get("adminId").toString());
        String oldPassword = params.get("oldPassword").toString();
        String newPassword = params.get("newPassword").toString();
        
        Admin admin = adminService.getById(adminId);
        if (admin == null) return Result.error("Admin not found");
        
        if (!admin.getPassword().equals(oldPassword)) {
            return Result.error("Old password incorrect");
        }
        
        admin.setPassword(newPassword);
        adminService.updateById(admin);
        return Result.success("Password changed");
    }

    // --- Audit Merchant ---
    @PostMapping("/audit/merchant")
    public Result<String> auditMerchant(@RequestParam Long merchantId, @RequestParam Integer status) {
        Merchant merchant = merchantService.getById(merchantId);
        if (merchant == null) return Result.error("Merchant not found");
        
        merchant.setAuditStatus(status); // 1: Pass, 2: Reject
        merchantService.updateById(merchant);
        return Result.success("Merchant audit completed");
    }

    @GetMapping("/merchant/pending")
    public Result<Page<Merchant>> listPendingMerchants(@RequestParam(defaultValue = "1") Integer pageNum,
                                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Merchant> page = new Page<>(pageNum, pageSize);
        return Result.success(merchantService.page(page, 
            new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getAuditStatus, 0) // Pending
                .orderByDesc(Merchant::getCreateTime)));
    }
    
    // --- User Management ---
    
    @PostMapping("/user/ban")
    public Result<?> banUser(@RequestParam String role, 
                             @RequestParam Long userId, 
                             @RequestParam String reason, 
                             @RequestParam Integer durationDays) {
        LocalDateTime endTime = LocalDateTime.now().plusDays(durationDays);
        if ("student".equals(role)) {
            Student s = studentService.getById(userId);
            if (s != null) {
                s.setStatus(0);
                s.setBanReason(reason);
                s.setBanEndTime(endTime);
                studentService.updateById(s);
            }
        } else if ("merchant".equals(role)) {
            Merchant m = merchantService.getById(userId);
            if (m != null) {
                m.setStatus(0);
                m.setBanReason(reason);
                m.setBanEndTime(endTime);
                merchantService.updateById(m);
            }
        }
        return Result.success("User banned successfully");
    }

    @PostMapping("/user/unban")
    public Result<?> unbanUser(@RequestParam String role, @RequestParam Long userId) {
        if ("student".equals(role)) {
            Student s = studentService.getById(userId);
            if (s != null) {
                s.setStatus(1);
                s.setBanReason(null);
                s.setBanEndTime(null);
                studentService.updateById(s);
            }
        } else if ("merchant".equals(role)) {
            Merchant m = merchantService.getById(userId);
            if (m != null) {
                m.setStatus(1);
                m.setBanReason(null);
                m.setBanEndTime(null);
                merchantService.updateById(m);
            }
        }
        return Result.success("User unbanned successfully");
    }

    @RequestMapping(value = "/user/status", method = {RequestMethod.GET, RequestMethod.POST})
    public Result<Map<String, Object>> getUserStatus(@RequestParam String role, @RequestParam Long userId) {
        Map<String, Object> status = new HashMap<>();
        if ("student".equals(role)) {
            Student s = studentService.getById(userId);
            if (s != null) {
                status.put("status", s.getStatus());
                status.put("banReason", s.getBanReason());
                status.put("banEndTime", s.getBanEndTime());
            } else {
                return Result.error("Student not found");
            }
        } else if ("merchant".equals(role)) {
            Merchant m = merchantService.getById(userId);
            if (m != null) {
                status.put("status", m.getStatus());
                status.put("banReason", m.getBanReason());
                status.put("banEndTime", m.getBanEndTime());
            } else {
                return Result.error("Merchant not found");
            }
        } else {
            return Result.error("Invalid role");
        }
        return Result.success(status);
    }
    
    @GetMapping("/user/list")
    public Result<Page<?>> listUsers(@RequestParam String role, 
                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        if ("student".equals(role)) {
            Page<Student> page = new Page<>(pageNum, pageSize);
            return Result.success(studentService.page(page));
        } else {
            Page<Merchant> page = new Page<>(pageNum, pageSize);
            return Result.success(merchantService.page(page));
        }
    }
    
    // --- Product Management ---
    
    @PostMapping("/product/audit")
    public Result<?> auditProduct(@RequestParam Long productId, @RequestParam Integer status) {
        Product p = productService.getById(productId);
        if (p == null) return Result.error("Product not found");
        p.setAuditStatus(status);
        productService.updateById(p);
        return Result.success("Product audited");
    }
    
    @PostMapping("/product/offshell")
    public Result<?> offshellProduct(@RequestParam Long productId) {
        Product p = productService.getById(productId);
        if (p == null) return Result.error("Product not found");
        p.setStatus(0); // Off shell
        productService.updateById(p);
        return Result.success("Product off-shelved");
    }
    
    // --- System Management ---
    
    @PostMapping("/announcement/publish")
    public Result<?> publishAnnouncement(@RequestBody Announcement announcement) {
        announcementMapper.insert(announcement);
        return Result.success("Announcement published");
    }

    @PostMapping("/announcement/delete")
    public Result<?> deleteAnnouncement(@RequestParam Long id) {
        announcementMapper.deleteById(id);
        return Result.success("Announcement deleted");
    }
    
    @GetMapping("/announcement/list")
    public Result<List<Announcement>> listAnnouncements() {
        return Result.success(announcementMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Announcement>().orderByDesc("create_time")));
    }
    
    @PostMapping("/config/update")
    public Result<?> updateConfig(@RequestBody SysConfig config) {
        SysConfig exist = sysConfigMapper.selectById(config.getConfigKey());
        if (exist != null) {
            exist.setConfigValue(config.getConfigValue());
            sysConfigMapper.updateById(exist);
        } else {
            sysConfigMapper.insert(config);
        }
        return Result.success("Config updated");
    }
    
    @GetMapping("/config/list")
    public Result<List<SysConfig>> listConfigs() {
        return Result.success(sysConfigMapper.selectList(null));
    }

    // --- Complaint Management ---

    @GetMapping("/complaint/list")
    public Result<Page<Complaint>> listComplaints(@RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                                  @RequestParam(required = false) Integer status) {
        Page<Complaint> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Complaint> query = new LambdaQueryWrapper<>();
        if (status != null) {
            query.eq(Complaint::getStatus, status);
        }
        query.orderByDesc(Complaint::getCreateTime);
        return Result.success(complaintMapper.selectPage(page, query));
    }

    @PostMapping("/complaint/resolve")
    public Result<?> resolveComplaint(@RequestBody Complaint complaint) {
        Complaint exist = complaintMapper.selectById(complaint.getComplaintId());
        if (exist == null) return Result.error("Complaint not found");
        
        exist.setStatus(complaint.getStatus()); // 1: Resolved, 2: Rejected
        exist.setResultNote(complaint.getResultNote());
        complaintMapper.updateById(exist);
        return Result.success("Complaint processed");
    }

    // --- Profile Audit ---

    @GetMapping("/profile/audit/list")
    public Result<Page<ProfileUpdateAudit>> listProfileAudits(@RequestParam(defaultValue = "1") Integer pageNum,
                                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<ProfileUpdateAudit> page = new Page<>(pageNum, pageSize);
        return Result.success(profileUpdateAuditMapper.selectPage(page, 
            new LambdaQueryWrapper<ProfileUpdateAudit>()
                .eq(ProfileUpdateAudit::getStatus, 0)
                .orderByDesc(ProfileUpdateAudit::getCreateTime)));
    }

    @PostMapping("/profile/audit/process")
    public Result<?> processProfileAudit(@RequestParam Long auditId, @RequestParam Integer status) {
        ProfileUpdateAudit audit = profileUpdateAuditMapper.selectById(auditId);
        if (audit == null) return Result.error("Audit record not found");
        
        audit.setStatus(status);
        profileUpdateAuditMapper.updateById(audit);
        
        if (status == 1) { // Pass, apply changes
            if ("student".equals(audit.getRole())) {
                Student s = studentService.getById(audit.getUserId());
                if ("school_id".equals(audit.getFieldName())) s.setSchoolId(audit.getNewValue());
                // Add other fields logic if needed
                studentService.updateById(s);
            } else if ("merchant".equals(audit.getRole())) {
                Merchant m = merchantService.getById(audit.getUserId());
                if ("shop_name".equals(audit.getFieldName())) m.setShopName(audit.getNewValue());
                merchantService.updateById(m);
            }
        }
        return Result.success("Audit processed");
    }

    // --- Statistics ---

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Simple counts
        stats.put("totalStudents", studentService.count());
        stats.put("totalMerchants", merchantService.count());
        stats.put("totalOrders", orderService.count());
        
        // TODO: More complex stats by time (week/month) require custom SQL or wrapper logic
        // For MVP, just returning totals
        return Result.success(stats);
    }

    // --- Login Logs ---

    @GetMapping("/login/logs")
    public Result<Page<AdminLoginLog>> listLoginLogs(@RequestParam(defaultValue = "1") Integer pageNum,
                                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                                     @RequestParam(required = false) String username) {
        Page<AdminLoginLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AdminLoginLog> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) wrapper.like(AdminLoginLog::getAdminUsername, username);
        wrapper.orderByDesc(AdminLoginLog::getLoginTime);
        return Result.success(adminLoginLogMapper.selectPage(page, wrapper));
    }
}
