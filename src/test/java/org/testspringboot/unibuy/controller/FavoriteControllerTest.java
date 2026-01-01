package org.testspringboot.unibuy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.testspringboot.unibuy.common.Result;
import org.testspringboot.unibuy.entity.Product;
import org.testspringboot.unibuy.entity.Student;
import org.testspringboot.unibuy.mapper.ProductMapper;
import org.testspringboot.unibuy.mapper.StudentMapper;
import org.testspringboot.unibuy.service.IFavoriteService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class FavoriteControllerTest {

    @Autowired
    private FavoriteController favoriteController;

    @Autowired
    private IFavoriteService favoriteService;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private ProductMapper productMapper;

    private Long testStudentId;
    private Long testProductId;

    @BeforeEach
    void setUp() {
        // Create test student
        long random = System.currentTimeMillis();
        Student student = new Student();
        student.setUsername("test_user_" + random);
        student.setPassword("password");
        student.setPhone("138" + (random % 100000000L));
        student.setSchoolId("sid_" + random);
        student.setNickname("Test Student");
        student.setBalance(BigDecimal.ZERO);
        studentMapper.insert(student);
        testStudentId = student.getStudentId();

        // Create test product
        Product product = new Product();
        product.setMerchantId(1L);
        product.setName("Test Product");
        product.setCategory("Test");
        product.setProxyPrice(new BigDecimal("99.99"));
        product.setStock(100);
        product.setAuditStatus(1);
        product.setStatus(1);
        product.setSourceRequestId(0L);
        productMapper.insert(product);
        testProductId = product.getProductId();
    }

    @Test
    void testAddFavoriteWithValidInputs() {
        Map<String, Long> params = new HashMap<>();
        params.put("studentId", testStudentId);
        params.put("productId", testProductId);

        Result<String> result = favoriteController.addFavorite(params);

        assertEquals(200, result.getCode());
        assertTrue(favoriteService.isFavorited(testStudentId, testProductId));
    }

    @Test
    void testAddFavoriteWithMissingStudentId() {
        Map<String, Long> params = new HashMap<>();
        params.put("productId", testProductId);

        Result<String> result = favoriteController.addFavorite(params);

        assertEquals(500, result.getCode());
        assertTrue(result.getMsg().contains("required"));
    }

    @Test
    void testAddFavoriteWithInvalidProductId() {
        Map<String, Long> params = new HashMap<>();
        params.put("studentId", testStudentId);
        params.put("productId", 99999L); // Non-existent product

        Result<String> result = favoriteController.addFavorite(params);

        assertEquals(500, result.getCode());
    }

    @Test
    void testGetFavoriteList() {
        // Add favorite
        favoriteService.addFavorite(testStudentId, testProductId);

        // Get list
        Result<Page<Product>> result = favoriteController.getFavoriteList(testStudentId, 1, 10);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getRecords().size());
        assertEquals(testProductId, result.getData().getRecords().get(0).getProductId());
    }

    @Test
    void testRemoveFavorite() {
        // First add a favorite
        favoriteService.addFavorite(testStudentId, testProductId);
        assertTrue(favoriteService.isFavorited(testStudentId, testProductId));

        // Then remove it
        Result<String> result = favoriteController.removeFavorite(testStudentId, testProductId);

        assertEquals(200, result.getCode());
        assertFalse(favoriteService.isFavorited(testStudentId, testProductId));
    }

    @Test
    void testCheckFavoriteWhenFavorited() {
        favoriteService.addFavorite(testStudentId, testProductId);

        Result<Boolean> result = favoriteController.checkFavorite(testStudentId, testProductId);

        assertEquals(200, result.getCode());
        assertTrue(result.getData());
    }

    @Test
    void testCheckFavoriteWhenNotFavorited() {
        Result<Boolean> result = favoriteController.checkFavorite(testStudentId, testProductId);

        assertEquals(200, result.getCode());
        assertFalse(result.getData());
    }

    @Test
    void testGetFavoriteListWithPagination() {
        // Add multiple favorites
        for (int i = 0; i < 5; i++) {
            Product product = new Product();
            product.setMerchantId(1L);
            product.setName("Test Product " + i);
            product.setCategory("Test");
            product.setProxyPrice(new BigDecimal("99.99"));
            product.setStock(100);
            product.setAuditStatus(1);
            product.setStatus(1);
            product.setSourceRequestId(0L);
            productMapper.insert(product);
            favoriteService.addFavorite(testStudentId, product.getProductId());
        }

        Result<Page<Product>> result = favoriteController.getFavoriteList(testStudentId, 1, 10);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(5, result.getData().getRecords().size());
    }

    @Test
    void testGetFavoriteListWhenEmpty() {
        Result<Page<Product>> result = favoriteController.getFavoriteList(testStudentId, 1, 10);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(0, result.getData().getRecords().size());
    }

    @Test
    void testGetFavoriteCount() {
        // Add 3 favorites
        for (int i = 0; i < 3; i++) {
            Product product = new Product();
            product.setMerchantId(1L);
            product.setName("Test Product " + i);
            product.setCategory("Test");
            product.setProxyPrice(new BigDecimal("99.99"));
            product.setStock(100);
            product.setAuditStatus(1);
            product.setStatus(1);
            product.setSourceRequestId(0L);
            productMapper.insert(product);
            favoriteService.addFavorite(testStudentId, product.getProductId());
        }

        Result<Long> result = favoriteController.getFavoriteCount(testStudentId);

        assertEquals(200, result.getCode());
        assertEquals(3L, result.getData());
    }

    @Test
    void testGetFavoriteCountWhenZero() {
        Result<Long> result = favoriteController.getFavoriteCount(testStudentId);

        assertEquals(200, result.getCode());
        assertEquals(0L, result.getData());
    }
}
