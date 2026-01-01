package org.testspringboot.unibuy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.testspringboot.unibuy.common.Result;
import org.testspringboot.unibuy.entity.Product;
import org.testspringboot.unibuy.service.IFavoriteService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private IFavoriteService favoriteService;

    /**
     * Add a product to favorites
     * POST /favorite/add
     */
    @PostMapping("/add")
    public Result<String> addFavorite(@RequestBody Map<String, Long> params) {
        try {
            Long studentId = params.get("studentId");
            Long productId = params.get("productId");
            
            if (studentId == null || productId == null) {
                return Result.error("Student ID and Product ID are required");
            }
            
            favoriteService.addFavorite(studentId, productId);
            return Result.success("Favorite added successfully");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * Remove a product from favorites
     * DELETE /favorite/remove
     */
    @DeleteMapping("/remove")
    public Result<String> removeFavorite(@RequestParam Long studentId, 
                                         @RequestParam Long productId) {
        try {
            if (studentId == null || productId == null) {
                return Result.error("Student ID and Product ID are required");
            }
            
            favoriteService.removeFavorite(studentId, productId);
            return Result.success("Favorite removed successfully");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * Check if a product is favorited
     * GET /favorite/check
     */
    @GetMapping("/check")
    public Result<Boolean> checkFavorite(@RequestParam Long studentId, 
                                         @RequestParam Long productId) {
        try {
            if (studentId == null || productId == null) {
                return Result.error("Student ID and Product ID are required");
            }
            
            boolean isFavorited = favoriteService.isFavorited(studentId, productId);
            return Result.success(isFavorited);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * Get paginated list of favorited products
     * GET /favorite/list
     */
    @GetMapping("/list")
    public Result<Page<Product>> getFavoriteList(@RequestParam Long studentId,
                                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            if (studentId == null) {
                return Result.error("Student ID is required");
            }
            
            Page<Product> page = new Page<>(pageNum, pageSize);
            Page<Product> result = favoriteService.getFavoriteProducts(studentId, page);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * Get total count of favorites for a student
     * GET /favorite/count
     */
    @GetMapping("/count")
    public Result<Long> getFavoriteCount(@RequestParam Long studentId) {
        try {
            if (studentId == null) {
                return Result.error("Student ID is required");
            }
            
            Long count = favoriteService.countByStudentId(studentId);
            return Result.success(count);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
