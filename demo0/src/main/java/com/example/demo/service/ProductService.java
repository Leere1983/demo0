package com.example.demo.service;

import com.example.demo.entity.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductService {

    Product createProduct(Product product);

    Product updateProduct(Long id, Product product);

    void deleteProduct(Long id);

    Optional<Product> getProductById(Long id);

    List<Product> getAllProducts();

    List<Product> searchProductsByName(String name);

    List<Product> getProductsByStatus(Integer status);

    List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    boolean decreaseStock(Long productId, Integer quantity);

    void increaseStock(Long productId, Integer quantity);
}