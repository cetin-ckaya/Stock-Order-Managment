package com.hibernate.stockordermanagment.service;

import com.hibernate.stockordermanagment.dto.request.CreateOrderRequest;
import com.hibernate.stockordermanagment.dto.request.OrderItemRequest;
import com.hibernate.stockordermanagment.entity.Product;
import com.hibernate.stockordermanagment.enums.ProductCategory;
import com.hibernate.stockordermanagment.enums.ProductStatus;
import com.hibernate.stockordermanagment.exception.InsufficientStockException;
import com.hibernate.stockordermanagment.repository.OrderRepository;
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
public class OrderTransactionTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Long product1Id;
    private Long product2Id;
    private Long product3Id;

    @BeforeEach
    void setUp() {
        long timestamp = System.currentTimeMillis();

        Product p1 = productRepository.save(Product.builder()
                .name("Transaction Test Urun 1 - " + timestamp)
                .category(ProductCategory.ELECTRONICS)
                .price(new BigDecimal("100.00"))
                .stockQuantity(10)
                .minimumStockLevel(0)
                .status(ProductStatus.ACTIVE)
                .build());

        Product p2 = productRepository.save(Product.builder()
                .name("Transaction Test Urun 2 - " + timestamp)
                .category(ProductCategory.BOOK)
                .price(new BigDecimal("50.00"))
                .stockQuantity(10)
                .minimumStockLevel(0)
                .status(ProductStatus.ACTIVE)
                .build());

        Product p3 = productRepository.save(Product.builder()
                .name("Transaction Test Urun 3 - " + timestamp)
                .category(ProductCategory.FOOD)
                .price(new BigDecimal("25.00"))
                .stockQuantity(1)
                .minimumStockLevel(0)
                .status(ProductStatus.ACTIVE)
                .build());

        product1Id = p1.getId();
        product2Id = p2.getId();
        product3Id = p3.getId();
    }

    @Test
    void ucuncuUrunStokYetersizseTumIslemRollbackOlmali() {
        long orderCountBefore = orderRepository.count();

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName("Rollback Test Musterisi");

        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(product1Id);
        item1.setQuantity(2);

        OrderItemRequest item2 = new OrderItemRequest();
        item2.setProductId(product2Id);
        item2.setQuantity(3);

        OrderItemRequest item3 = new OrderItemRequest();
        item3.setProductId(product3Id);
        item3.setQuantity(100);

        request.setItems(List.of(item1, item2, item3));

        assertThrows(InsufficientStockException.class, () -> orderService.createOrder(request));

        Product p1After = productRepository.findById(product1Id).orElseThrow();
        Product p2After = productRepository.findById(product2Id).orElseThrow();
        Product p3After = productRepository.findById(product3Id).orElseThrow();

        assertEquals(10, p1After.getStockQuantity(), "Urun 1 stogu degismemeliydi");
        assertEquals(10, p2After.getStockQuantity(), "Urun 2 stogu degismemeliydi");
        assertEquals(1, p3After.getStockQuantity(), "Urun 3 stogu degismemeliydi");

        long orderCountAfter = orderRepository.count();
        assertEquals(orderCountBefore, orderCountAfter, "Siparis kaydi olusmamaliydi");
    }
}