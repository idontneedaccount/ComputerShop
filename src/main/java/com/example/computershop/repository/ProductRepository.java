package com.example.computershop.repository;
import com.example.computershop.dto.ProductSalesDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.computershop.entity.Products;

public interface ProductRepository extends JpaRepository<Products, String> {
    boolean existsByName(String name);
    
    @Query("SELECT DISTINCT p.brand FROM Products p WHERE p.brand IS NOT NULL ORDER BY p.brand")
    List<String> findDistinctBrands();
     @Query("SELECT p FROM Products p ORDER BY p.quantity DESC")
    List<Products> findTop5ProductsByStock(Pageable pageable);

    @Query("SELECT new com.example.computershop.dto.ProductSalesDTO(od.product, SUM(od.quantity)) " +
            "FROM OrderDetail od " +
            "GROUP BY od.product " +
            "ORDER BY SUM(od.quantity) DESC")
    List<ProductSalesDTO> findTop5BestSellingProducts(Pageable pageable);

    @Query("SELECT COUNT(p) FROM Products p")
    long countProducts();
}