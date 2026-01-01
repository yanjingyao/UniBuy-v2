package org.testspringboot.unibuy.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.testspringboot.unibuy.entity.OrderItem;
import org.testspringboot.unibuy.entity.Orders;
import java.util.List;

public interface IOrderService extends IService<Orders> {
    Orders createOrder(Orders order, List<OrderItem> items);
    void payOrder(Long orderId);
    void acceptOrder(Long orderId);
    void uploadVoucher(Long orderId, String voucherUrl);
    void finishOrder(Long orderId);
    void cancelOrder(Long orderId, String reason);
    Orders getOrderDetail(Long orderId);
    List<OrderItem> listItems(Long orderId);
    Page<Orders> listOrders(Page<Orders> page, Long userId, String role, Integer status);
}
