package com.example.computershop.repository;
import com.example.computershop.dto.ProductSalesDTO;
import org.springframework.data.domain.Pageable;

import com.example.computershop.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Products, String> {
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