package com.example.demo0.service;

import com.example.demo0.entity.Product;
import com.example.demo0.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("100.00"));
        testProduct.setStock(10);
        testProduct.setCategory("Test Category");
    }

    @Test
    void testFindById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        
        Optional<Product> found = productService.findById(1L);
        
        assertTrue(found.isPresent());
        assertEquals("Test Product", found.get().getName());
    }

    @Test
    void testFindAll() {
        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product 2");
        
        when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct, product2));
        
        List<Product> products = productService.findAll();
        
        assertEquals(2, products.size());
    }

    @Test
    void testFindByCategory() {
        when(productRepository.findByCategory("Test Category")).thenReturn(Arrays.asList(testProduct));
        
        List<Product> products = productService.findByCategory("Test Category");
        
        assertEquals(1, products.size());
        assertEquals("Test Product", products.get(0).getName());
    }

    @Test
    void testCreateProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        
        Product created = productService.create(testProduct);
        
        assertNotNull(created);
        assertEquals("Test Product", created.getName());
    }

    @Test
    void testUpdateProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        
        testProduct.setName("Updated Product");
        Product updated = productService.update(1L, testProduct);
        
        assertNotNull(updated);
        assertEquals("Updated Product", updated.getName());
    }

    @Test
    void testDecreaseStock() {
        when(productRepository.decreaseStock(1L, 2)).thenReturn(1);
        
        boolean success = productService.decreaseStock(1L, 2);
        
        assertTrue(success);
    }

    @Test
    void testDecreaseStockInsufficient() {
        when(productRepository.decreaseStock(1L, 20)).thenReturn(0);
        
        boolean success = productService.decreaseStock(1L, 20);
        
        assertFalse(success);
    }

    @Test
    void testIncreaseStock() {
        when(productRepository.increaseStock(1L, 5)).thenReturn(1);
        
        assertDoesNotThrow(() -> productService.increaseStock(1L, 5));
        verify(productRepository, times(1)).increaseStock(1L, 5);
    }

    @Test
    void testDeleteProduct() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);
        
        assertDoesNotThrow(() -> productService.delete(1L));
    }
}