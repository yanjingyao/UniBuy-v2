package org.testspringboot.unibuy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.testspringboot.unibuy.entity.*;
import org.testspringboot.unibuy.mapper.RequestResponseMapper;
import org.testspringboot.unibuy.mapper.UserRequestMapper;
import org.testspringboot.unibuy.service.IOrderService;
import org.testspringboot.unibuy.service.IProductService;
import org.testspringboot.unibuy.service.IRequestService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.util.StringUtils;

@Service
public class RequestServiceImpl extends ServiceImpl<UserRequestMapper, UserRequest> implements IRequestService {

    @Autowired
    private RequestResponseMapper responseMapper;

    @Autowired
    private IProductService productService;

    @Autowired
    private IOrderService orderService;

    @Override
    public void publishRequest(UserRequest request) {
        request.setStatus(0); // Pending
        
        // Calculate deadline based on urgency
        LocalDateTime now = LocalDateTime.now();
        if (request.getUrgencyLevel() == 3) { // High
            request.setDeadline(now.plusHours(24));
        } else if (request.getUrgencyLevel() == 2) { // Med
            request.setDeadline(now.plusDays(3));
        } else { // Low
            request.setDeadline(now.plusDays(5));
        }
        
        this.save(request);
    }

    @Override
    public Page<UserRequest> listSquare(Page<UserRequest> page, String category) {
        // Show pending or responding requests, not expired/completed
        LambdaQueryWrapper<UserRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UserRequest::getStatus, 0, 1);
        
        if (StringUtils.hasText(category)) {
            wrapper.eq(UserRequest::getCategory, category);
        }
        
        wrapper.orderByDesc(UserRequest::getUrgencyLevel); // High urgency first
        return this.page(page, wrapper);
    }

    @Override
    public void respondRequest(RequestResponse response) {
        response.setIsSelected(0);
        responseMapper.insert(response);
        
        // Update request status to Responding (1)
        UserRequest req = this.getById(response.getRequestId());
        if (req != null && req.getStatus() == 0) {
            req.setStatus(1);
            this.updateById(req);
        }
    }

    @Override
    public List<RequestResponse> listResponses(Long requestId) {
        return responseMapper.selectList(new LambdaQueryWrapper<RequestResponse>()
                .eq(RequestResponse::getRequestId, requestId));
    }

    @Override
    @Transactional
    public Long confirmSelection(Long requestId, Long responseId) {
        // 1. Update Request Status
        UserRequest req = this.getById(requestId);
        req.setStatus(2); // Accepted
        this.updateById(req);

        // 2. Update Response Selected
        RequestResponse resp = responseMapper.selectById(responseId);
        resp.setIsSelected(1);
        responseMapper.updateById(resp);

        // 3. Create Product (Auto Approved & Hidden/Targeted)
        // Note: As per requirement, merchant does not need to publish product manually.
        // We create a hidden/internal product just to satisfy Order Item reference constraint.
        Product product = new Product();
        product.setMerchantId(resp.getMerchantId());
        product.setName(req.getProductName() + " [定制]");
        product.setCategory(req.getCategory());
        product.setOriginalPrice(req.getExpectedPrice()); 
        
        // Use Quoted Price + Service Fee (if any)
        BigDecimal total = resp.getQuotedPrice().add(resp.getServiceFee() != null ? resp.getServiceFee() : BigDecimal.ZERO);
        product.setProxyPrice(total);
        
        product.setStock(req.getMinJoinUsers() > 1 ? req.getMinJoinUsers() : 1);
        product.setChannel("Request Conversion");
        product.setImages(req.getRefImage());
        product.setAuditStatus(1); // Auto Approved
        product.setSourceRequestId(requestId);
        // Change status to 0 (Down/Hidden) so it doesn't appear in public mall
        // but still valid for order history
        product.setStatus(0); 
        
        productService.save(product);
        
        // 4. Create Order Directly
        Orders order = new Orders();
        order.setStudentId(req.getStudentId());
        order.setMerchantId(resp.getMerchantId());
        // Fix: Explicitly set totalPrice
        order.setTotalPrice(product.getProxyPrice()); 
        // totalPrice will be calculated by createOrder or we set it here.
        // Usually createOrder logic handles validation, but here we construct it.
        // Let's rely on createOrder to calc from items.
        
        OrderItem item = new OrderItem();
        item.setProductId(product.getProductId());
        item.setQuantity(1); // Request usually implies 1 unit per person unless specified
        item.setPrice(product.getProxyPrice());
        item.setProductName(product.getName());
        item.setProductImg(product.getImages());
        
        Orders createdOrder = orderService.createOrder(order, Collections.singletonList(item));
        return createdOrder.getOrderId();
    }

    @Override
    public List<UserRequest> listByStudent(Long studentId) {
        return this.list(new LambdaQueryWrapper<UserRequest>()
                .eq(UserRequest::getStudentId, studentId)
                .orderByDesc(UserRequest::getRequestId));
    }
}
