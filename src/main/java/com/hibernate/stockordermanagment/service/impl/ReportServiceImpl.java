package com.hibernate.stockordermanagment.service.impl;

import com.hibernate.stockordermanagment.dto.response.OrderSummaryResponse;
import com.hibernate.stockordermanagment.dto.response.TopSellingProductResponse;
import com.hibernate.stockordermanagment.entity.Order;
import com.hibernate.stockordermanagment.entity.Product;
import com.hibernate.stockordermanagment.enums.OrderStatus;
import com.hibernate.stockordermanagment.repository.OrderItemRepository;
import com.hibernate.stockordermanagment.repository.OrderRepository;
import com.hibernate.stockordermanagment.repository.ProductRepository;
import com.hibernate.stockordermanagment.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Override
    public List<TopSellingProductResponse> getTopSellingProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> rawResults = orderItemRepository.findTopSellingProductsRaw(pageable);

        return rawResults.stream()
                .map(row -> {
                    Long productId = (Long) row[0];
                    Long totalQuantity = (Long) row[1];
                    BigDecimal totalRevenue = (BigDecimal) row[2];

                    String productName = productRepository.findById(productId)
                            .map(Product::getName)
                            .orElse("Bilinmeyen Ürün");

                    return new TopSellingProductResponse(productId, productName, totalQuantity, totalRevenue);
                })
                .collect(Collectors.toList());
    }

    @Override
    public OrderSummaryResponse getOrderSummary() {
        List<Order> allOrders = orderRepository.findAll();

        long totalOrderCount = allOrders.size();

        long completedOrderCount = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .count();

        long cancelledOrderCount = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.CANCELLED)
                .count();

        long createdOrderCount = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.CREATED)
                .count();

        BigDecimal totalRevenue = allOrders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageOrderAmount = totalOrderCount > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrderCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return OrderSummaryResponse.builder()
                .totalOrderCount(totalOrderCount)
                .completedOrderCount(completedOrderCount)
                .cancelledOrderCount(cancelledOrderCount)
                .createdOrderCount(createdOrderCount)
                .totalRevenue(totalRevenue)
                .averageOrderAmount(averageOrderAmount)
                .build();
    }
}