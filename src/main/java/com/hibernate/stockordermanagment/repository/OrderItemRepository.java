package com.hibernate.stockordermanagment.repository;

import com.hibernate.stockordermanagment.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi FROM OrderItem oi WHERE oi.productId = :productId")
    List<OrderItem> findByProductId(@Param("productId") Long productId);

    @Query("""
        SELECT oi.productId, SUM(oi.quantity), SUM(oi.totalPrice)
        FROM OrderItem oi
        GROUP BY oi.productId
        ORDER BY SUM(oi.quantity) DESC
    """)
    List<Object[]> findTopSellingProducts();
}