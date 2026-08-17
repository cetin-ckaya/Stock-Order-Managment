package com.hibernate.stockordermanagment.service;

import com.hibernate.stockordermanagment.dto.request.CreateOrderRequest;
import com.hibernate.stockordermanagment.dto.request.OrderItemRequest;
import com.hibernate.stockordermanagment.dto.response.OrderResponse;
import com.hibernate.stockordermanagment.entity.Order;
import com.hibernate.stockordermanagment.entity.Product;
import com.hibernate.stockordermanagment.enums.ProductCategory;
import com.hibernate.stockordermanagment.enums.ProductStatus;
import com.hibernate.stockordermanagment.exception.InsufficientStockException;
import com.hibernate.stockordermanagment.repository.OrderRepository;
import com.hibernate.stockordermanagment.repository.ProductRepository;
import com.hibernate.stockordermanagment.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PricingService pricingService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Test Ürün")
                .category(ProductCategory.ELECTRONICS)
                .price(new BigDecimal("100.00"))
                .stockQuantity(10)
                .minimumStockLevel(2)
                .status(ProductStatus.ACTIVE)
                .build();
    }

    @Test
    void ayniUrunIkiKezGonderilirseMiktarlarBirlestirilir() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName("Ahmet Yılmaz");

        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(1L);
        item1.setQuantity(2);

        OrderItemRequest item2 = new OrderItemRequest();
        item2.setProductId(1L);
        item2.setQuantity(3);

        request.setItems(List.of(item1, item2));

        when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(pricingService.calculateTotal(any())).thenReturn(new BigDecimal("500.00"));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        OrderResponse response = orderService.createOrder(request);

        assertEquals(1, response.getItems().size());
        assertEquals(5, response.getItems().get(0).getQuantity());
    }

    @Test
    void yetersizStokVarsaExceptionOlusurVeSiparisKaydedilmez() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName("Ayşe Kaya");

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(100);
        request.setItems(List.of(item));

        when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.of(product));

        assertThrows(InsufficientStockException.class,
                () -> orderService.createOrder(request));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void siparisTutariDogruHesaplanir() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName("Mehmet Demir");

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(3);
        request.setItems(List.of(item));

        when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(pricingService.calculateTotal(any())).thenReturn(new BigDecimal("300.00"));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        OrderResponse response = orderService.createOrder(request);

        assertEquals(new BigDecimal("300.00"), response.getTotalAmount());
    }

    @Test
    void yeterliStokVarsaSiparisOlusturulur() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName("Zeynep Şahin");

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(5);
        request.setItems(List.of(item));

        when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(pricingService.calculateTotal(any())).thenReturn(new BigDecimal("500.00"));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        OrderResponse response = orderService.createOrder(request);

        assertEquals("Zeynep Şahin", response.getCustomerName());
        verify(orderRepository, times(1)).save(any(Order.class));
    }
}