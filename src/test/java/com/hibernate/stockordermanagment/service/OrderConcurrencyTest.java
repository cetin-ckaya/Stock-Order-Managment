package com.hibernate.stockordermanagment.service;

import com.hibernate.stockordermanagment.dto.request.CreateOrderRequest;
import com.hibernate.stockordermanagment.dto.request.OrderItemRequest;
import com.hibernate.stockordermanagment.entity.Product;
import com.hibernate.stockordermanagment.enums.ProductCategory;
import com.hibernate.stockordermanagment.enums.ProductStatus;
import com.hibernate.stockordermanagment.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class OrderConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    private Long testProductId;

    @BeforeEach
    void setUp() {
        Product product = Product.builder()
                .name("Test Ürün Concurrency " + System.currentTimeMillis())
                .description("Concurrency testi için ürün")
                .category(ProductCategory.ELECTRONICS)
                .price(new BigDecimal("100.00"))
                .stockQuantity(1)
                .minimumStockLevel(0)
                .status(ProductStatus.ACTIVE)
                .build();

        Product saved = productRepository.save(product);
        testProductId = saved.getId();
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteById(testProductId);
    }

    @Test
    void stoktaBirAdetVarkenIkiEsZamanliSiparisSadeceBiriBasarili() throws InterruptedException {
        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int userId = i;
            executorService.submit(() -> {
                try {
                    CreateOrderRequest request = new CreateOrderRequest();
                    request.setCustomerName("Kullanici-" + userId);

                    OrderItemRequest itemRequest = new OrderItemRequest();
                    itemRequest.setProductId(testProductId);
                    itemRequest.setQuantity(1);

                    request.setItems(List.of(itemRequest));

                    orderService.createOrder(request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Product finalProduct = productRepository.findById(testProductId).orElseThrow();

        assertEquals(1, successCount.get(), "Sadece 1 sipariş başarılı olmalıydı");
        assertEquals(1, failCount.get(), "1 sipariş yetersiz stok nedeniyle başarısız olmalıydı");
        assertEquals(0, finalProduct.getStockQuantity(), "Stok 0 olmalı, eksiye düşmemeli");
        assertTrue(finalProduct.getStockQuantity() >= 0, "Stok asla negatif olmamalı");
    }
}