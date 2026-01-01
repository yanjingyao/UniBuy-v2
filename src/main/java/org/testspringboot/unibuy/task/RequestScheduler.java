package org.testspringboot.unibuy.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.testspringboot.unibuy.entity.UserRequest;
import org.testspringboot.unibuy.service.IRequestService;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RequestScheduler {

    @Autowired
    private IRequestService requestService;

    // Run every minute
    @Scheduled(cron = "0 * * * * ?")
    public void checkRequestTimeout() {
        LocalDateTime now = LocalDateTime.now();
        
        // Find requests that are Pending(0) or Responding(1) AND deadline < now
        LambdaQueryWrapper<UserRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UserRequest::getStatus, 0, 1);
        wrapper.lt(UserRequest::getDeadline, now);
        
        List<UserRequest> expiredRequests = requestService.list(wrapper);
        
        if (!expiredRequests.isEmpty()) {
            for (UserRequest req : expiredRequests) {
                req.setStatus(3); // Expired
                // In a real app, send notification to student here
            }
            requestService.updateBatchById(expiredRequests);
            System.out.println("Expired " + expiredRequests.size() + " requests at " + now);
        }
    }
}
