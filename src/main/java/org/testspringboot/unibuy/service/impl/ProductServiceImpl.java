package org.testspringboot.unibuy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.testspringboot.unibuy.entity.Product;
import org.testspringboot.unibuy.mapper.ProductMapper;
import org.testspringboot.unibuy.service.IProductService;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

    @Override
    public Page<Product> listForStudent(Page<Product> page, String keyword, String category, String sort) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1); // Must be Up
        wrapper.eq(Product::getAuditStatus, 1); // Must be Audited
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Product::getName, keyword)
                    .or()
                    .like(Product::getCategory, keyword)); // TODO: Join merchant name if needed, but simplistic now
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(Product::getCategory, category);
        }
        
        if ("price_asc".equals(sort)) {
            wrapper.orderByAsc(Product::getProxyPrice);
        } else if ("price_desc".equals(sort)) {
            wrapper.orderByDesc(Product::getProxyPrice);
        } else {
            wrapper.orderByDesc(Product::getCreateTime);
        }
        return this.page(page, wrapper);
    }

    @Override
    public void publishProduct(Product product) {
        product.setAuditStatus(0); // Pending
        product.setStatus(0); // Default Down until audited
        this.save(product);
    }

    @Override
    public void updateProduct(Product product) {
        Product old = this.getById(product.getProductId());
        if (old == null) throw new RuntimeException("Product not found");
        
        old.setName(product.getName());
        old.setCategory(product.getCategory());
        old.setSpecs(product.getSpecs());
        old.setOriginalPrice(product.getOriginalPrice());
        old.setProxyPrice(product.getProxyPrice());
        old.setStock(product.getStock());
        old.setChannel(product.getChannel());
        old.setDescription(product.getDescription());
        if (StringUtils.hasText(product.getImages())) {
            old.setImages(product.getImages());
        }
        
        // If critical info changed, maybe reset audit? For now, keep simple.
        this.updateById(old);
    }

    @Override
    public void updateStatus(Long productId, Integer status) {
        Product product = this.getById(productId);
        if (product != null) {
            product.setStatus(status);
            this.updateById(product);
        }
    }

    @Override
    public void auditProduct(Long productId, Integer status) {
        Product product = this.getById(productId);
        if (product == null) return;
        product.setAuditStatus(status);
        if (status == 1) {
            product.setStatus(1); // Auto Up if passed? Or let merchant do it. Requirement says "Display after audit". So auto Up.
        } else {
            product.setStatus(0);
        }
        this.updateById(product);
    }
}
