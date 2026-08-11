package com.hibernate.stockordermanagment.dto.request;

import com.hibernate.stockordermanagment.enums.ProductCategory;
import com.hibernate.stockordermanagment.enums.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Ürün Güncelleme Request DTO
 *
 * TASK: PUT /api/v1/products/{id}
 *
 * Update işlemlerinde tüm alanlar opsiyoneldir
 * Sadece gönderilen alanlar güncellenecek (Partial Update)
 *
 * NOT: @NotNull annotation'ları yok çünkü opsiyonel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductRequest {

    // Tüm alanlar opsiyonel (null olabilir)
    // Gönderilmeyen alanlar güncellenmez

    @Size(min = 2, max = 200, message = "Ürün adı 2-200 karakter arasında olmalıdır")
    private String name;

    @Size(max = 1000, message = "Açıklama maksimum 1000 karakter olabilir")
    private String description;

    private ProductCategory category;

    @DecimalMin(value = "0.01", message = "Fiyat sıfırdan büyük olmalıdır")
    @Digits(integer = 17, fraction = 2, message = "Fiyat formatı hatalı")
    private BigDecimal price;

    @Min(value = 0, message = "Stok miktarı negatif olamaz")
    private Integer stockQuantity;

    @Min(value = 0, message = "Minimum stok seviyesi negatif olamaz")
    private Integer minimumStockLevel;

    private ProductStatus status;
}