package com.hibernate.stockordermanagment.dto.response;

import com.hibernate.stockordermanagment.enums.ProductCategory;
import com.hibernate.stockordermanagment.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ürün Response DTO
 *
 * TASK KURALI: "Entity neden doğrudan API response olarak dönülmemelidir?"
 *
 * NEDEN ENTITY KULLANMIYORUZ?
 * 1. GÜVENLIK: Entity'de hassas bilgiler olabilir (version, internal_id vb.)
 * 2. BAĞIMLILIK: Frontend entity yapısına bağımlı olur, değişiklik zor
 * 3. PERFORMANS: Lazy loading, circular reference gibi JPA sorunları
 * 4. API CONTRACT: API response farklı format isteyebilir
 * 5. ESNEKLIK: Aynı entity farklı formatlarda dönülebilir
 *
 * ÖRNEK:
 * - ProductResponse: Temel bilgiler
 * - ProductDetailResponse: İlişkili verilerle birlikte
 * - ProductSummaryResponse: Sadece id ve name
 *
 * DTO PATTERN:
 * - Data Transfer Object
 * - Katmanlar arası veri taşıma
 * - Controller ↔ Service ↔ Repository
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private ProductCategory category;
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer minimumStockLevel;
    private ProductStatus status;

    /**
     * Düşük Stok Uyarısı Flag
     * Frontend'de badge göstermek için kullanışlı
     *
     * NOT: Entity'de isLowStock() metodu var
     * Mapper içinde hesaplanacak
     */
    private Boolean isLowStock;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}