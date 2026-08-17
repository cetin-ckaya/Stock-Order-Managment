package com.hibernate.stockordermanagment.controller;

import com.hibernate.stockordermanagment.dto.response.OrderSummaryResponse;
import com.hibernate.stockordermanagment.dto.response.TopSellingProductResponse;
import com.hibernate.stockordermanagment.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Raporlama endpointleri")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/top-selling-products")
    @Operation(summary = "En çok satılan ürünler")
    public ResponseEntity<List<TopSellingProductResponse>> getTopSellingProducts(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(reportService.getTopSellingProducts(limit));
    }

    @GetMapping("/order-summary")
    @Operation(summary = "Sipariş özet raporu")
    public ResponseEntity<OrderSummaryResponse> getOrderSummary() {
        return ResponseEntity.ok(reportService.getOrderSummary());
    }
}