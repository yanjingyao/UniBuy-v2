package org.testspringboot.unibuy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.testspringboot.unibuy.common.Result;
import org.testspringboot.unibuy.entity.Product;
import org.testspringboot.unibuy.service.IProductService;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private IProductService productService;

    // Student: List Products
    @GetMapping("/list")
    public Result<Page<Product>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String category,
                                      @RequestParam(required = false) String sort) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        return Result.success(productService.listForStudent(page, keyword, category, sort));
    }

    // Student: Detail
    @GetMapping("/detail")
    public Result<Product> detail(@RequestParam Long id) {
        return Result.success(productService.getById(id));
    }

    // Merchant: Publish
    @PostMapping("/publish")
    public Result<String> publish(@RequestBody Product product) {
        productService.publishProduct(product);
        return Result.success("Product published, waiting for audit");
    }

    // Merchant: Update
    @PostMapping("/update")
    public Result<String> update(@RequestBody Product product) {
        productService.updateProduct(product);
        return Result.success("Product updated");
    }

    // Merchant: Status (Up/Down)
    @PostMapping("/status")
    public Result<String> updateStatus(@RequestParam Long productId, @RequestParam Integer status) {
        productService.updateStatus(productId, status);
        return Result.success("Status updated");
    }

    // Admin: Audit
    @PostMapping("/audit")
    public Result<String> audit(@RequestParam Long productId, @RequestParam Integer status) {
        productService.auditProduct(productId, status);
        return Result.success("Audit completed");
    }

    // Admin: Pending Products
    @GetMapping("/admin/pending")
    public Result<Page<Product>> listPending(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        return Result.success(productService.page(page,
            new LambdaQueryWrapper<Product>()
                .eq(Product::getAuditStatus, 0)
                .orderByDesc(Product::getCreateTime)));
    }

    // Merchant: My Products
    @GetMapping("/merchant/list")
    public Result<Page<Product>> myProducts(@RequestParam Long merchantId,
                                            @RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        return Result.success(productService.page(page, 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, merchantId)
                .orderByDesc(Product::getCreateTime)));
    }
}
