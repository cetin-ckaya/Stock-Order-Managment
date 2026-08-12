package com.hibernate.stockordermanagment.repository;

import com.hibernate.stockordermanagment.entity.Order;
import com.hibernate.stockordermanagment.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByCustomerName(String customerName);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status != :status")
    BigDecimal sumTotalAmountExcludingStatus(@Param("status") OrderStatus status);

    long countByStatus(OrderStatus status);
}