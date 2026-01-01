package org.testspringboot.unibuy.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.testspringboot.unibuy.entity.Product;

public interface IProductService extends IService<Product> {
    Page<Product> listForStudent(Page<Product> page, String keyword, String category, String sort);
    void publishProduct(Product product);
    void updateProduct(Product product);
    void updateStatus(Long productId, Integer status);
    void auditProduct(Long productId, Integer status); // 1:Pass, 2:Reject
}
