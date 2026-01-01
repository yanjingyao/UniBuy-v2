package org.testspringboot.unibuy.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.testspringboot.unibuy.entity.Orders;
import org.testspringboot.unibuy.service.IOrderService;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderScheduler {

    @Autowired
    private IOrderService orderService;

    // Check every minute
    @Scheduled(cron = "0 * * * * ?")
    public void checkOrderTimeout() {
        LocalDateTime deadline = LocalDateTime.now().minusHours(24);
        
        // Find orders created > 24h ago AND status = 0 (WaitPay)
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getOrderStatus, 0);
        wrapper.lt(Orders::getCreateTime, deadline);
        
        List<Orders> expiredOrders = orderService.list(wrapper);
        if (!expiredOrders.isEmpty()) {
            for (Orders order : expiredOrders) {
                orderService.cancelOrder(order.getOrderId(), "Payment timeout (24h)");
            }
            System.out.println("Cancelled " + expiredOrders.size() + " unpaid orders.");
        }
    }
}
