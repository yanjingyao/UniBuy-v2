package org.testspringboot.unibuy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.testspringboot.unibuy.common.Result;
import org.testspringboot.unibuy.entity.OrderItem;
import org.testspringboot.unibuy.entity.Orders;
import org.testspringboot.unibuy.entity.Student;
import org.testspringboot.unibuy.entity.Merchant;
import org.testspringboot.unibuy.service.IOrderService;
import org.testspringboot.unibuy.service.IStudentService;
import org.testspringboot.unibuy.service.IMerchantService;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;
    
    @Autowired
    private IStudentService studentService;
    
    @Autowired
    private IMerchantService merchantService;

    @PostMapping("/create")
    public Result<Orders> create(@RequestBody CreateOrderRequest request) {
        // Pass address to service or set it here
        if (request.getAddress() != null) {
            request.getOrder().setAddressSnapshot(request.getAddress());
        }
        return Result.success(orderService.createOrder(request.getOrder(), request.getItems()));
    }

    @PostMapping("/pay")
    public Result<String> pay(@RequestParam Long orderId) {
        orderService.payOrder(orderId);
        return Result.success("Payment successful");
    }

    @PostMapping("/accept")
    public Result<String> accept(@RequestParam Long orderId) {
        orderService.acceptOrder(orderId);
        return Result.success("Order accepted");
    }

    @PostMapping("/voucher")
    public Result<String> voucher(@RequestParam Long orderId, @RequestParam String url) {
        orderService.uploadVoucher(orderId, url);
        return Result.success("Voucher uploaded");
    }

    @PostMapping("/finish")
    public Result<String> finish(@RequestParam Long orderId) {
        orderService.finishOrder(orderId);
        return Result.success("Order finished");
    }

    @PostMapping("/cancel")
    public Result<String> cancel(@RequestParam Long orderId, @RequestParam(required = false) String reason) {
        Orders order = orderService.getById(orderId);
        if (order == null) return Result.error("Order not found");
        
        // 0:WaitPay, 1:WaitAccept
        if (order.getOrderStatus() < 2) {
             orderService.cancelOrder(orderId, reason);
             return Result.success("Order cancelled");
        } else {
             // Request Admin Cancel
             order.setOrderStatus(5); // Dispute / Pending Cancel
             order.setCancelReason(reason);
             orderService.updateById(order);
             return Result.success("Cancellation request submitted to Admin");
        }
    }
    
    @PostMapping("/admin/cancel")
    public Result<String> adminCancel(@RequestParam Long orderId) {
        Orders order = orderService.getById(orderId);
        if (order == null) return Result.error("Order not found");
        
        order.setOrderStatus(4); // Cancelled
        // refund logic should be here (omitted for MVP)
        orderService.updateById(order);
        return Result.success("Order cancelled by Admin");
    }

    @PostMapping("/admin/resolveDispute")
    public Result<String> resolveDispute(@RequestParam Long orderId, 
                                         @RequestParam String action, // "refund" or "reject"
                                         @RequestParam(required = false) Integer targetStatus, // if reject, restore to this status
                                         @RequestParam(required = false) String adminNote) {
        Orders order = orderService.getById(orderId);
        if (order == null) return Result.error("Order not found");
        
        if ("refund".equals(action)) {
            order.setOrderStatus(4); // Cancelled
            // Trigger actual refund
            studentService.recharge(order.getStudentId(), order.getTotalPrice(), "Refund");
            order.setCancelReason(order.getCancelReason() + " [Admin Refund: " + (adminNote != null ? adminNote : "") + "]");
        } else if ("reject".equals(action)) {
            if (targetStatus == null) targetStatus = 2; // Default back to WaitPickup/Processing
            order.setOrderStatus(targetStatus);
            // Notify student?
        } else {
            return Result.error("Invalid action");
        }
        
        orderService.updateById(order);
        return Result.success("Dispute resolved");
    }

    @PostMapping("/updateAddress")
    public Result<String> updateAddress(@RequestParam Long orderId, @RequestParam String address) {
        Orders order = orderService.getById(orderId);
        if (order == null) return Result.error("Order not found");
        
        // Only allow update if order is not completed or cancelled
        if (order.getOrderStatus() == 3 || order.getOrderStatus() == 4) {
            return Result.error("Cannot modify address for completed or cancelled orders");
        }
        
        order.setAddressSnapshot(address);
        orderService.updateById(order);
        return Result.success("Address updated");
    }

    @GetMapping("/items")
    public Result<List<OrderItem>> getItems(@RequestParam Long orderId) {
        return Result.success(orderService.listItems(orderId));
    }

    @GetMapping("/list")
    public Result<Page<org.testspringboot.unibuy.entity.vo.OrderVO>> list(@RequestParam Long userId,
                                     @RequestParam String role,
                                     @RequestParam(required = false) Integer status,
                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Orders> page = new Page<>(pageNum, pageSize);
        Page<Orders> orderPage = orderService.listOrders(page, userId, role, status);
        
        // Convert to VO
        Page<org.testspringboot.unibuy.entity.vo.OrderVO> voPage = new Page<>(pageNum, pageSize);
        BeanUtils.copyProperties(orderPage, voPage, "records");
        
        List<Orders> records = orderPage.getRecords();
        List<org.testspringboot.unibuy.entity.vo.OrderVO> vos = new ArrayList<>();
        
        if (records != null && !records.isEmpty()) {
            // Fetch Names if Admin
            Map<Long, String> studentNames = null;
            Map<Long, String> merchantNames = null;
            
            if ("admin".equals(role)) {
                List<Long> sIds = records.stream().map(Orders::getStudentId).distinct().collect(Collectors.toList());
                List<Long> mIds = records.stream().map(Orders::getMerchantId).distinct().collect(Collectors.toList());
                
                if (!sIds.isEmpty()) {
                    List<Student> students = studentService.listByIds(sIds);
                    studentNames = students.stream().collect(Collectors.toMap(Student::getStudentId, 
                        s -> s.getNickname() != null ? s.getNickname() : s.getUsername()));
                }
                if (!mIds.isEmpty()) {
                    List<Merchant> merchants = merchantService.listByIds(mIds);
                    merchantNames = merchants.stream().collect(Collectors.toMap(Merchant::getMerchantId, Merchant::getShopName));
                }
            }
            
            for (Orders o : records) {
                org.testspringboot.unibuy.entity.vo.OrderVO vo = new org.testspringboot.unibuy.entity.vo.OrderVO();
                BeanUtils.copyProperties(o, vo);
                if ("admin".equals(role)) {
                    if (studentNames != null) vo.setStudentName(studentNames.getOrDefault(o.getStudentId(), "未知用户"));
                    if (merchantNames != null) vo.setMerchantName(merchantNames.getOrDefault(o.getMerchantId(), "未知商家"));
                }
                vos.add(vo);
            }
        }
        voPage.setRecords(vos);
        voPage.setTotal(orderPage.getTotal());
        
        return Result.success(voPage);
    }

    @Data
    static class CreateOrderRequest {
        private Orders order;
        private List<OrderItem> items;
        private String address; // Full address snapshot
    }
}
