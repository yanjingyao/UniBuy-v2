package org.testspringboot.unibuy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LongRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testspringboot.unibuy.entity.Favorite;
import org.testspringboot.unibuy.entity.Product;
import org.testspringboot.unibuy.entity.Student;
import org.testspringboot.unibuy.mapper.FavoriteMapper;
import org.testspringboot.unibuy.mapper.ProductMapper;
import org.testspringboot.unibuy.mapper.StudentMapper;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for Favorite Service
 * Feature: student-favorites
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:mysql://localhost:3306/unibuy_test?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false"
})
public class FavoriteServicePropertyTest {

    @Autowired
    private IFavoriteService favoriteService;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     * Feature: student-favorites, Property 1: Favorite creation persistence
     * For any student ID and product ID, when a favorite is added, 
     * immediately querying the database should return a favorite record with those exact IDs.
     * Validates: Requirements 1.1, 1.4
     */
    @Property(tries = 100)
    @Transactional
    void favoriteCreationPersistence(
            @ForAll @LongRange(min = 1, max = 1000) long studentIdSeed,
            @ForAll @LongRange(min = 1, max = 1000) long productIdSeed) {
        
        // Setup: Create test student and product
        Student student = createTestStudent(studentIdSeed);
        Product product = createTestProduct(productIdSeed);
        
        Long studentId = student.getStudentId();
        Long productId = product.getProductId();
        
        // Action: Add favorite
        favoriteService.addFavorite(studentId, productId);
        
        // Verification: Query database should return the favorite record
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getStudentId, studentId)
               .eq(Favorite::getProductId, productId);
        
        Favorite favorite = favoriteMapper.selectOne(wrapper);
        
        // Assert: Favorite record exists with correct IDs
        assertNotNull(favorite, "Favorite record should exist in database");
        assertEquals(studentId, favorite.getStudentId(), "Student ID should match");
        assertEquals(productId, favorite.getProductId(), "Product ID should match");
        assertNotNull(favorite.getCreateTime(), "Create time should be set");
    }

    /**
     * Feature: student-favorites, Property 2: Favorite idempotency (uniqueness)
     * For any student and product combination, adding a favorite multiple times 
     * should result in exactly one favorite record in the database.
     * Validates: Requirements 1.3
     */
    @Property(tries = 100)
    @Transactional
    void favoriteIdempotency(
            @ForAll @LongRange(min = 1, max = 1000) long studentIdSeed,
            @ForAll @LongRange(min = 1, max = 1000) long productIdSeed,
            @ForAll @IntRange(min = 2, max = 5) int addAttempts) {
        
        // Setup: Create test student and product
        Student student = createTestStudent(studentIdSeed);
        Product product = createTestProduct(productIdSeed);
        
        Long studentId = student.getStudentId();
        Long productId = product.getProductId();
        
        // Action: Add favorite multiple times
        for (int i = 0; i < addAttempts; i++) {
            favoriteService.addFavorite(studentId, productId);
        }
        
        // Verification: Count records in database
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getStudentId, studentId)
               .eq(Favorite::getProductId, productId);
        
        long count = favoriteMapper.selectCount(wrapper);
        
        // Assert: Exactly one record should exist
        assertEquals(1, count, 
            "Should have exactly one favorite record after " + addAttempts + " add attempts");
    }

    /**
     * Feature: student-favorites, Property 3: Favorite removal completeness
     * For any existing favorite record, when it is removed, subsequent queries 
     * to the database should not return that favorite record.
     * Validates: Requirements 2.1, 2.3
     */
    @Property(tries = 100)
    @Transactional
    void favoriteRemovalCompleteness(
            @ForAll @LongRange(min = 1, max = 1000) long studentIdSeed,
            @ForAll @LongRange(min = 1, max = 1000) long productIdSeed) {
        
        // Setup: Create test student, product, and favorite
        Student student = createTestStudent(studentIdSeed);
        Product product = createTestProduct(productIdSeed);
        
        Long studentId = student.getStudentId();
        Long productId = product.getProductId();
        
        // Add favorite first
        favoriteService.addFavorite(studentId, productId);
        
        // Verify it exists
        assertTrue(favoriteService.isFavorited(studentId, productId), 
            "Favorite should exist before removal");
        
        // Action: Remove favorite
        favoriteService.removeFavorite(studentId, productId);
        
        // Verification: Query database should not return the record
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getStudentId, studentId)
               .eq(Favorite::getProductId, productId);
        
        long count = favoriteMapper.selectCount(wrapper);
        
        // Assert: No record should exist
        assertEquals(0, count, "Favorite record should be permanently deleted from database");
        assertFalse(favoriteService.isFavorited(studentId, productId), 
            "isFavorited should return false after removal");
    }

    /**
     * Feature: student-favorites, Property 4: Favorite status consistency
     * For any student-product pair, the favorite status returned by the check API 
     * should be true if and only if a favorite record exists in the database.
     * Validates: Requirements 4.1, 4.2, 4.3
     */
    @Property(tries = 100)
    @Transactional
    void favoriteStatusConsistency(
            @ForAll @LongRange(min = 1, max = 1000) long studentIdSeed,
            @ForAll @LongRange(min = 1, max = 1000) long productIdSeed,
            @ForAll boolean shouldFavorite) {
        
        // Setup: Create test student and product
        Student student = createTestStudent(studentIdSeed);
        Product product = createTestProduct(productIdSeed);
        
        Long studentId = student.getStudentId();
        Long productId = product.getProductId();
        
        // Action: Conditionally add favorite
        if (shouldFavorite) {
            favoriteService.addFavorite(studentId, productId);
        }
        
        // Verification: Check database state
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getStudentId, studentId)
               .eq(Favorite::getProductId, productId);
        
        boolean existsInDatabase = favoriteMapper.selectCount(wrapper) > 0;
        boolean apiReturnsTrue = favoriteService.isFavorited(studentId, productId);
        
        // Assert: API status should match database state
        assertEquals(existsInDatabase, apiReturnsTrue, 
            "isFavorited() should return true if and only if record exists in database");
        assertEquals(shouldFavorite, apiReturnsTrue, 
            "Favorite status should match whether we added it");
    }

    /**
     * Feature: student-favorites, Property 5: Favorite list query correctness
     * For any student, the list of products returned by the favorites API should contain 
     * exactly those products that have corresponding favorite records in the database.
     * Validates: Requirements 3.1, 3.2, 3.4
     */
    @Property(tries = 100)
    @Transactional
    void favoriteListQueryCorrectness(
            @ForAll @LongRange(min = 1, max = 1000) long studentIdSeed,
            @ForAll @IntRange(min = 1, max = 10) int favoriteCount) {
        
        // Setup: Create test student
        Student student = createTestStudent(studentIdSeed);
        Long studentId = student.getStudentId();
        
        // Create multiple products and favorite them
        java.util.Set<Long> expectedProductIds = new java.util.HashSet<>();
        for (int i = 0; i < favoriteCount; i++) {
            Product product = createTestProduct(studentIdSeed * 1000 + i);
            favoriteService.addFavorite(studentId, product.getProductId());
            expectedProductIds.add(product.getProductId());
        }
        
        // Action: Query favorite products
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Product> page = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 20);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Product> result = 
            favoriteService.getFavoriteProducts(studentId, page);
        
        // Verification: Extract product IDs from result
        java.util.Set<Long> actualProductIds = result.getRecords().stream()
            .map(Product::getProductId)
            .collect(java.util.stream.Collectors.toSet());
        
        // Assert: Returned products should match favorited products exactly
        assertEquals(expectedProductIds.size(), actualProductIds.size(), 
            "Number of returned products should match number of favorites");
        assertEquals(expectedProductIds, actualProductIds, 
            "Returned product IDs should exactly match favorited product IDs");
        
        // Verify each returned product has required fields
        for (Product product : result.getRecords()) {
            assertNotNull(product.getName(), "Product name should be present");
            assertNotNull(product.getProxyPrice(), "Product price should be present");
            assertNotNull(product.getProductId(), "Product ID should be present");
        }
    }

    // Helper method to create test student
    private Student createTestStudent(long seed) {
        Student student = new Student();
        student.setUsername("test_student_" + seed);
        student.setPassword("password");
        student.setPhone("1380000" + String.format("%04d", seed % 10000));
        student.setSchoolId("2024" + String.format("%06d", seed));
        student.setNickname("Test Student " + seed);
        student.setBalance(BigDecimal.ZERO);
        studentMapper.insert(student);
        return student;
    }

    // Helper method to create test product
    private Product createTestProduct(long seed) {
        Product product = new Product();
        product.setMerchantId(1L); // Assume merchant 1 exists
        product.setName("Test Product " + seed);
        product.setCategory("Test");
        product.setProxyPrice(new BigDecimal("99.99"));
        product.setStock(100);
        product.setAuditStatus(1); // Approved
        product.setStatus(1); // Up
        product.setSourceRequestId(0L);
        productMapper.insert(product);
        return product;
    }
}
