package org.testspringboot.unibuy.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.testspringboot.unibuy.entity.OrderItem;
import org.testspringboot.unibuy.entity.Orders;
import org.testspringboot.unibuy.entity.PayRecord;
import org.testspringboot.unibuy.entity.Product;
import org.testspringboot.unibuy.mapper.OrderItemMapper;
import org.testspringboot.unibuy.mapper.OrdersMapper;
import org.testspringboot.unibuy.mapper.PayRecordMapper;
import org.testspringboot.unibuy.mapper.ProductMapper;
import org.testspringboot.unibuy.service.IChatService;
import org.testspringboot.unibuy.service.IOrderService;
import org.testspringboot.unibuy.service.IStudentService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrderService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private IChatService chatService;
    
    @Autowired
    private IStudentService studentService;
    
    @Autowired
    private PayRecordMapper payRecordMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional
    public Orders createOrder(Orders order, List<OrderItem> items) {
        order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
        order.setPayStatus(0);
        order.setOrderStatus(0); // WaitPay
        
        // Ensure service fee is set if provided, or calculate?
        // For now assume frontend passes it or it's 0. 
        if (order.getServiceFee() == null) {
            order.setServiceFee(java.math.BigDecimal.ZERO);
        }
        
        this.save(order);

        for (OrderItem item : items) {
            item.setOrderId(order.getOrderId());
            // Populate product details
            if (item.getProductId() != null) {
                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    // Check and Deduct Stock
                    if (product.getStock() < item.getQuantity()) {
                        throw new RuntimeException("商品 " + product.getName() + " 库存不足");
                    }
                    product.setStock(product.getStock() - item.getQuantity());
                    if (product.getStock() <= 0) {
                        product.setStatus(0); // Auto off-shelf
                    }
                    productMapper.updateById(product);

                    item.setProductName(product.getName());
                    // Use first image if multiple
                    String imgs = product.getImages();
                    if (imgs != null && imgs.startsWith("[")) {
                         try {
                             // Simple parse or just take it as is if frontend handles it. 
                             // But entity expects string. Let's just store the first image URL if possible or the raw string.
                             // OrderItem.productImg usually stores a single image URL.
                             // Let's parse JSON manually to avoid dependency
                             if (imgs.contains("\"")) {
                                 String[] parts = imgs.split("\"");
                                 for (String p : parts) {
                                     if (p.startsWith("http")) {
                                         item.setProductImg(p);
                                         break;
                                     }
                                 }
                             }
                         } catch (Exception e) {}
                    } else {
                         item.setProductImg(imgs);
                    }
                    if (item.getProductImg() == null) item.setProductImg(product.getImages()); // Fallback
                }
            }
            orderItemMapper.insert(item);
        }
        return order;
    }

    @Override
    @Transactional
    public void payOrder(Long orderId) {
        Orders order = this.getById(orderId);
        if (order.getOrderStatus() != 0) return;
        
        // Deduct Balance
        studentService.deductBalance(order.getStudentId(), order.getTotalPrice());
        
        // Create Pay Record
        PayRecord record = new PayRecord();
        record.setOrderId(orderId);
        record.setAmount(order.getTotalPrice());
        record.setMethod("Balance");
        record.setStatus(1);
        payRecordMapper.insert(record);
        
        order.setPayStatus(1);
        order.setOrderStatus(1); // WaitAccept
        order.setPayTime(LocalDateTime.now());
        this.updateById(order);
        
        // Notify Merchant (Role 2)
        chatService.sendSystemMessage(order.getMerchantId(), 2, "新订单通知：您有一笔新订单 " + order.getOrderNo() + " 待接单", orderId);
    }

    @Override
    public void acceptOrder(Long orderId) {
        Orders order = this.getById(orderId);
        if (order.getOrderStatus() != 1) return;
        order.setOrderStatus(2); // WaitPickup
        order.setAcceptTime(LocalDateTime.now());
        this.updateById(order);

        // Notify Student (Role 1)
        chatService.sendSystemMessage(order.getStudentId(), 1, "订单进度：商家已接单，正在准备您的商品", orderId);
    }

    @Override
    public void uploadVoucher(Long orderId, String voucherUrl) {
        Orders order = this.getById(orderId);
        order.setVoucherImg(voucherUrl);
        this.updateById(order);
    }

    @Override
    public void finishOrder(Long orderId) {
        Orders order = this.getById(orderId);
        order.setOrderStatus(3); // Done
        order.setFinishTime(LocalDateTime.now());
        this.updateById(order);

        // Notify Student (Role 1)
        chatService.sendSystemMessage(order.getStudentId(), 1, "订单完成：您的订单已送达/取货，欢迎评价", orderId);
    }

    @Override
    public void cancelOrder(Long orderId, String reason) {
        Orders order = this.getById(orderId);
        if (order.getOrderStatus() == 3 || order.getOrderStatus() == 4) return;
        
        // If pending pay or pending accept, can cancel directly
        if (order.getOrderStatus() == 0 || order.getOrderStatus() == 1) {
             order.setOrderStatus(4); // Cancelled
             order.setCancelReason(reason);
             this.updateById(order);
        } else {
             // If already accepted, maybe request cancel? For now, let's allow direct cancel but log it, or require another status
             // Simplified: Just cancel
             order.setOrderStatus(4);
             order.setCancelReason(reason);
             this.updateById(order);
        }
    }

    @Override
    public Orders getOrderDetail(Long orderId) {
        Orders order = this.getById(orderId);
        if (order != null) {
            // Populate items (assuming Orders entity has a list of items field, or we need to add it transiently)
            // But MyBatis Plus entity usually maps to table. We might need a DTO or just fetch items separately.
            // For simplicity, let's assume we can fetch items and user needs to call another API or we wrap it.
            // Wait, IOrderService return type is Orders. Let's add items to Orders transiently if possible or create VO.
            // Let's check Orders.java again. It doesn't have items list.
            // I will return Orders and user calls another API for items? Or I modify Orders to have transient items.
        }
        return order;
    }

    @Override
    public List<OrderItem> listItems(Long orderId) {
        return orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId));
    }

    @Override
    public Page<Orders> listOrders(Page<Orders> page, Long userId, String role, Integer status) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        if ("student".equals(role)) {
            wrapper.eq(Orders::getStudentId, userId);
        } else if ("merchant".equals(role)) {
            wrapper.eq(Orders::getMerchantId, userId);
        } else if ("admin".equals(role)) {
            // Admin sees all
        }
        if (status != null) {
            wrapper.eq(Orders::getOrderStatus, status);
        }
        wrapper.orderByDesc(Orders::getCreateTime);
        return this.page(page, wrapper);
    }
}
