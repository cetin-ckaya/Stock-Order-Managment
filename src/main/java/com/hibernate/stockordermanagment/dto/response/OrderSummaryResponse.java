package com.hibernate.stockordermanagment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSummaryResponse {
    private long totalOrderCount;
    private long completedOrderCount;
    private long cancelledOrderCount;
    private long createdOrderCount;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderAmount;
}