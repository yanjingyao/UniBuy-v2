package org.testspringboot.unibuy.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.testspringboot.unibuy.entity.RequestResponse;
import org.testspringboot.unibuy.entity.UserRequest;
import java.util.List;

public interface IRequestService extends IService<UserRequest> {
    void publishRequest(UserRequest request);
    Page<UserRequest> listSquare(Page<UserRequest> page, String category); // Request Square
    void respondRequest(RequestResponse response);
    List<RequestResponse> listResponses(Long requestId);
    Long confirmSelection(Long requestId, Long responseId); // Convert to Product & Order, return OrderId
    List<UserRequest> listByStudent(Long studentId);
}
