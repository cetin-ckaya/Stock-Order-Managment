package com.hibernate.stockordermanagment.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotBlank(message = "Müşteri adı boş olamaz")
    private String customerName;

    @NotEmpty(message = "Sipariş en az bir ürün içermelidir")
    @Valid
    private List<OrderItemRequest> items;
}