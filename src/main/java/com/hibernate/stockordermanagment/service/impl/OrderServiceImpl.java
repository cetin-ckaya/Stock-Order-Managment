package com.hibernate.stockordermanagment.service.impl;

import com.hibernate.stockordermanagment.dto.request.CreateOrderRequest;
import com.hibernate.stockordermanagment.dto.request.OrderItemRequest;
import com.hibernate.stockordermanagment.dto.response.OrderItemResponse;
import com.hibernate.stockordermanagment.dto.response.OrderResponse;
import com.hibernate.stockordermanagment.entity.Order;
import com.hibernate.stockordermanagment.entity.OrderItem;
import com.hibernate.stockordermanagment.entity.Product;
import com.hibernate.stockordermanagment.enums.OrderStatus;
import com.hibernate.stockordermanagment.enums.ProductStatus;
import com.hibernate.stockordermanagment.exception.InsufficientStockException;
import com.hibernate.stockordermanagment.exception.InvalidOrderException;
import com.hibernate.stockordermanagment.exception.OrderNotFoundException;
import com.hibernate.stockordermanagment.exception.ProductNotActiveException;
import com.hibernate.stockordermanagment.exception.ProductNotFoundException;
import com.hibernate.stockordermanagment.repository.OrderRepository;
import com.hibernate.stockordermanagment.repository.ProductRepository;
import com.hibernate.stockordermanagment.service.OrderService;
import com.hibernate.stockordermanagment.service.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PricingService pricingService;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Sipariş oluşturuluyor, müşteri: {}", request.getCustomerName());

        // TASK - STREAM API: Aynı ürün iki kez gönderilmişse miktarları birleştir
        // Collectors.toMap kullanarak productId -> toplam miktar map'i oluşturuyoruz
        Map<Long, Integer> mergedItems = request.getItems().stream()
                .collect(Collectors.toMap(
                        OrderItemRequest::getProductId,
                        OrderItemRequest::getQuantity,
                        Integer::sum  // Aynı productId varsa miktarları topla
                ));

        log.info("Ürün listesi birleştirildi, toplam farklı ürün sayısı: {}", mergedItems.size());

        // Order nesnesi oluştur
        Order order = Order.builder()
                .customerName(request.getCustomerName())
                .status(OrderStatus.CREATED)
                .totalAmount(BigDecimal.ZERO)
                .build();

        // Her ürün için kontrol ve OrderItem oluşturma
        for (Map.Entry<Long, Integer> entry : mergedItems.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

            // 1. Ürün var mı?
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));

            // 2. Ürün aktif mi?
            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new ProductNotActiveException(productId, product.getStatus().name());
            }

            // 3. Yeterli stok var mı?
            if (product.getStockQuantity() < quantity) {
                throw new InsufficientStockException(productId, quantity, product.getStockQuantity());
            }

            // 4. Stok azalt
            product.decreaseStock(quantity);
            productRepository.save(product);

            // 5. OrderItem oluştur
            BigDecimal unitPrice = product.getPrice();
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));

            OrderItem orderItem = OrderItem.builder()
                    .productId(productId)
                    .quantity(quantity)
                    .unitPrice(unitPrice)
                    .totalPrice(totalPrice)
                    .build();

            order.addItem(orderItem);
        }

        // TASK - STREAM API + PricingService: Toplam tutar hesapla
        BigDecimal totalAmount = pricingService.calculateTotal(order.getItems());
        order.setTotalAmount(totalAmount);

        // 6. Siparişi kaydet
        Order savedOrder = orderRepository.save(order);
        log.info("Sipariş başarıyla oluşturuldu, id: {}", savedOrder.getId());

        return toResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return toResponse(order);
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        log.info("Sipariş iptal ediliyor, id: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // TASK KURALI: Zaten iptal edilmiş sipariş tekrar iptal edilemez
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderException(orderId, "Sipariş zaten iptal edilmiş");
        }

        // TASK KURALI: Tamamlanmış sipariş iptal edilemez
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new InvalidOrderException(orderId, "Tamamlanmış sipariş iptal edilemez");
        }

        // TASK: Stokları geri ekle
        order.getItems().forEach(item -> {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));
            product.increaseStock(item.getQuantity());
            productRepository.save(product);
        });

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);

        log.info("Sipariş iptal edildi, id: {}", orderId);
        return toResponse(savedOrder);
    }

    // Manuel mapping (OrderMapper yerine burada yaptık)
    private OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCustomerName(order.getCustomerName());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        // STREAM API: OrderItem listesini OrderItemResponse listesine dönüştür
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        response.setItems(itemResponses);
        return response;
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProductId());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setTotalPrice(item.getTotalPrice());
        return response;
    }
}