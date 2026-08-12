package com.hibernate.stockordermanagment.service;

import com.hibernate.stockordermanagment.entity.OrderItem;

import java.math.BigDecimal;
import java.util.List;

public interface PricingService {
    BigDecimal calculateTotal(List<OrderItem> items);
}