package org.testspringboot.unibuy.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.testspringboot.unibuy.entity.PayRecord;

public interface IPayService extends IService<PayRecord> {
    void processPayment(Long orderId, String method);
    Page<PayRecord> listHistory(Page<PayRecord> page, Long userId); // Need to join Order to verify user? Or store userId in PayRecord?
    // PayRecord only has orderId. So we need to query orders first or join.
    // For simplicity, let's assume we filter by orderIds belonging to user.
}
