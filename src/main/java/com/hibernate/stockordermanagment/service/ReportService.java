package com.hibernate.stockordermanagment.service;

import com.hibernate.stockordermanagment.dto.response.OrderSummaryResponse;
import com.hibernate.stockordermanagment.dto.response.TopSellingProductResponse;

import java.util.List;

public interface ReportService {
    List<TopSellingProductResponse> getTopSellingProducts(int limit);
    OrderSummaryResponse getOrderSummary();
}