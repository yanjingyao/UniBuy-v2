package org.testspringboot.unibuy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.testspringboot.unibuy.entity.Orders;
import org.testspringboot.unibuy.entity.PayRecord;
import org.testspringboot.unibuy.mapper.OrdersMapper;
import org.testspringboot.unibuy.mapper.PayRecordMapper;
import org.testspringboot.unibuy.service.IOrderService;
import org.testspringboot.unibuy.service.IPayService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PayServiceImpl extends ServiceImpl<PayRecordMapper, PayRecord> implements IPayService {

    @Autowired
    private IOrderService orderService;
    
    @Autowired
    private OrdersMapper ordersMapper;

    @Override
    @Transactional
    public void processPayment(Long orderId, String method) {
        Orders order = orderService.getById(orderId);
        if (order == null || order.getOrderStatus() != 0) {
            throw new RuntimeException("Order invalid or already paid");
        }

        // 1. Create Pay Record
        PayRecord record = new PayRecord();
        record.setOrderId(orderId);
        record.setAmount(order.getTotalPrice());
        record.setMethod(method);
        record.setStatus(1); // Success
        this.save(record);

        // 2. Update Order Status (Call existing logic)
        orderService.payOrder(orderId);
    }

    @Override
    public Page<PayRecord> listHistory(Page<PayRecord> page, Long userId) {
        // 1. Find all orderIds for this student
        List<Long> orderIds = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getStudentId, userId)
                .select(Orders::getOrderId))
                .stream().map(Orders::getOrderId).collect(Collectors.toList());

        if (orderIds.isEmpty()) {
            return new Page<>(page.getCurrent(), page.getSize());
        }

        // 2. Find pay records for these orders
        return this.page(page, new LambdaQueryWrapper<PayRecord>()
                .in(PayRecord::getOrderId, orderIds)
                .orderByDesc(PayRecord::getCreateTime));
    }
}
