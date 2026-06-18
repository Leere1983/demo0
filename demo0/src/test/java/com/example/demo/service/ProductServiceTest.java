package com.example.demo.service;

import com.example.demo.entity.Product;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateProduct() {
        Product product = Product.builder()
                .name("Test Product")
                .price(BigDecimal.valueOf(99.99))
                .stock(100)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product createdProduct = productService.createProduct(product);

        assertNotNull(createdProduct);
        assertEquals("Test Product", createdProduct.getName());
        assertEquals(BigDecimal.valueOf(99.99), createdProduct.getPrice());
    }

    @Test
    void testGetProductById() {
        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(BigDecimal.valueOf(99.99))
                .stock(100)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<Product> foundProduct = productService.getProductById(1L);

        assertTrue(foundProduct.isPresent());
        assertEquals("Test Product", foundProduct.get().getName());
    }

    @Test
    void testDecreaseStockSuccess() {
        when(productRepository.decreaseStock(1L, 10)).thenReturn(1);

        boolean result = productService.decreaseStock(1L, 10);

        assertTrue(result);
        verify(productRepository, times(1)).decreaseStock(1L, 10);
    }

    @Test
    void testDecreaseStockFailure() {
        when(productRepository.decreaseStock(1L, 10)).thenReturn(0);

        boolean result = productService.decreaseStock(1L, 10);

        assertFalse(result);
    }

    @Test
    void testIncreaseStock() {
        doNothing().when(productRepository).increaseStock(1L, 10);

        assertDoesNotThrow(() -> productService.increaseStock(1L, 10));
        verify(productRepository, times(1)).increaseStock(1L, 10);
    }

    @Test
    void testUpdateProduct() {
        Product existingProduct = Product.builder()
                .id(1L)
                .name("Old Name")
                .price(BigDecimal.valueOf(50.00))
                .build();

        Product updateProduct = Product.builder()
                .name("New Name")
                .price(BigDecimal.valueOf(75.00))
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        Product updatedProduct = productService.updateProduct(1L, updateProduct);

        assertNotNull(updatedProduct);
        verify(productRepository, times(1)).save(any(Product.class));
    }
}