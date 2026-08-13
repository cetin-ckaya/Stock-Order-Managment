package com.hibernate.stockordermanagment.repository;

import com.hibernate.stockordermanagment.entity.Product;
import com.hibernate.stockordermanagment.enums.ProductStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    boolean existsByNameAndStatus(String name, ProductStatus status);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.minimumStockLevel")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' ORDER BY p.price DESC")
    List<Product> findTop5ByOrderByPriceDesc();

    Optional<Product> findByNameAndStatus(String name, ProductStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);
}