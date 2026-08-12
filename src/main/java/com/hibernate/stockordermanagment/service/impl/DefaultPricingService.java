package com.hibernate.stockordermanagment.service.impl;

import com.hibernate.stockordermanagment.entity.OrderItem;
import com.hibernate.stockordermanagment.service.PricingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DefaultPricingService implements PricingService {

    @Override
    public BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}