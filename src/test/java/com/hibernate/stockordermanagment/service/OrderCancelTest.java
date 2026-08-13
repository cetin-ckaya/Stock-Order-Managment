package com.hibernate.stockordermanagment.service;

import com.hibernate.stockordermanagment.dto.request.CreateOrderRequest;
import com.hibernate.stockordermanagment.dto.request.OrderItemRequest;
import com.hibernate.stockordermanagment.dto.response.OrderResponse;
import com.hibernate.stockordermanagment.entity.Product;
import com.hibernate.stockordermanagment.enums.OrderStatus;
import com.hibernate.stockordermanagment.enums.ProductCategory;
import com.hibernate.stockordermanagment.enums.ProductStatus;
import com.hibernate.stockordermanagment.exception.InvalidOrderException;
import com.hibernate.stockordermanagment.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class OrderCancelTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    private Long productId;

    @BeforeEach
    void setUp() {
        Product product = productRepository.save(Product.builder()
                .name("Iptal Test Urun - " + System.currentTimeMillis())
                .category(ProductCategory.CLOTHING)
                .price(new BigDecimal("200.00"))
                .stockQuantity(10)
                .minimumStockLevel(0)
                .status(ProductStatus.ACTIVE)
                .build());

        productId = product.getId();
    }

    @Test
    void siparisIptalEdilinceStoklarGeriEklenmeli() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName("Iptal Test Musterisi");

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(productId);
        item.setQuantity(3);
        request.setItems(List.of(item));

        OrderResponse order = orderService.createOrder(request);

        Product afterOrder = productRepository.findById(productId).orElseThrow();
        assertEquals(7, afterOrder.getStockQuantity(), "Siparis sonrasi stok 7 olmali");

        OrderResponse cancelled = orderService.cancelOrder(order.getId());

        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());

        Product afterCancel = productRepository.findById(productId).orElseThrow();
        assertEquals(10, afterCancel.getStockQuantity(), "Iptal sonrasi stok 10'a donmeli");
    }

    @Test
    void iptalEdilmisSiparisTekrarIptalEdilemez() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName("Cift Iptal Test Musterisi");

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(productId);
        item.setQuantity(2);
        request.setItems(List.of(item));

        OrderResponse order = orderService.createOrder(request);

        orderService.cancelOrder(order.getId());

        assertThrows(InvalidOrderException.class, () -> orderService.cancelOrder(order.getId()));
    }
}