package com.hibernate.stockordermanagment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopSellingProductResponse {
    private Long productId;
    private String productName;
    private Long totalSoldQuantity;
    private BigDecimal totalRevenue;
}