package org.testspringboot.unibuy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.testspringboot.unibuy.entity.Favorite;
import org.testspringboot.unibuy.entity.Product;
import org.testspringboot.unibuy.mapper.FavoriteMapper;
import org.testspringboot.unibuy.mapper.ProductMapper;
import org.testspringboot.unibuy.mapper.StudentMapper;
import org.testspringboot.unibuy.service.IFavoriteService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements IFavoriteService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Override
    public void addFavorite(Long studentId, Long productId) {
        // Validate student exists
        if (studentMapper.selectById(studentId) == null) {
            throw new RuntimeException("Student not found");
        }

        // Validate product exists
        if (productMapper.selectById(productId) == null) {
            throw new RuntimeException("Product not found");
        }

        // Create favorite record
        Favorite favorite = new Favorite();
        favorite.setStudentId(studentId);
        favorite.setProductId(productId);

        try {
            this.save(favorite);
        } catch (DuplicateKeyException e) {
            // Idempotent operation - if already favorited, just return success
            // No need to throw exception
        }
    }

    @Override
    public void removeFavorite(Long studentId, Long productId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getStudentId, studentId)
               .eq(Favorite::getProductId, productId);
        
        // Idempotent operation - if not exists, just return success
        this.remove(wrapper);
    }

    @Override
    public boolean isFavorited(Long studentId, Long productId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getStudentId, studentId)
               .eq(Favorite::getProductId, productId);
        
        return this.count(wrapper) > 0;
    }

    @Override
    public Page<Product> getFavoriteProducts(Long studentId, Page<Product> page) {
        // 1. Query Favorite with pagination
        Page<Favorite> favoritePage = this.page(new Page<>(page.getCurrent(), page.getSize()),
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getStudentId, studentId)
                        .orderByDesc(Favorite::getCreateTime));
        
        if (favoritePage.getRecords().isEmpty()) {
            return new Page<>(page.getCurrent(), page.getSize(), favoritePage.getTotal());
        }

        // 2. Extract product IDs
        List<Long> productIds = favoritePage.getRecords().stream()
                .map(Favorite::getProductId)
                .collect(Collectors.toList());

        // 3. Query Products
        List<Product> products = productMapper.selectBatchIds(productIds);
        
        // 4. Sort products to match favorite order
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p));
        
        List<Product> sortedProducts = productIds.stream()
                .map(productMap::get)
                .filter(java.util.Objects::nonNull) // Handle deleted products
                .collect(Collectors.toList());
        
        // 5. Construct result Page
        Page<Product> resultPage = new Page<>(favoritePage.getCurrent(), favoritePage.getSize(), favoritePage.getTotal());
        resultPage.setRecords(sortedProducts);
        
        return resultPage;
    }

    @Override
    public Long countByStudentId(Long studentId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getStudentId, studentId);
        
        return this.count(wrapper);
    }
}
