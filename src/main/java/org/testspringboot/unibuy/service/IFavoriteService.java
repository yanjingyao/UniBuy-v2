package org.testspringboot.unibuy.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.testspringboot.unibuy.entity.Favorite;
import org.testspringboot.unibuy.entity.Product;

public interface IFavoriteService extends IService<Favorite> {
    /**
     * Add a product to student's favorites
     * @param studentId Student ID
     * @param productId Product ID
     */
    void addFavorite(Long studentId, Long productId);

    /**
     * Remove a product from student's favorites
     * @param studentId Student ID
     * @param productId Product ID
     */
    void removeFavorite(Long studentId, Long productId);

    /**
     * Check if a product is favorited by student
     * @param studentId Student ID
     * @param productId Product ID
     * @return true if favorited, false otherwise
     */
    boolean isFavorited(Long studentId, Long productId);

    /**
     * Get paginated list of favorited products for a student
     * @param studentId Student ID
     * @param page Page object
     * @return Page of products
     */
    Page<Product> getFavoriteProducts(Long studentId, Page<Product> page);

    /**
     * Count total favorites for a student
     * @param studentId Student ID
     * @return Total count
     */
    Long countByStudentId(Long studentId);
}
