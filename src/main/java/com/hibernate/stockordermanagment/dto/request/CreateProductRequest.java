package com.hibernate.stockordermanagment.dto.request;

import com.hibernate.stockordermanagment.enums.ProductCategory;
import jakarta.validation.constraints.*; // Bu satır zaten jakarta'daki NotNull'ı kapsıyor
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Ürün Oluşturma Request DTO
 *
 * TASK GEREKSİNİMİ: "POST /api/v1/products"
 *
 * ÖRNEK REQUEST:
 * {
 *   "name": "Kablosuz Kulaklık",
 *   "description": "Bluetooth destekli kulaklık",
 *   "category": "ELECTRONICS",
 *   "price": 1500.00,
 *   "stockQuantity": 20,
 *   "minimumStockLevel": 5
 * }
 *
 * TASK KURALLAR:
 * ✓ "Ürün adı boş olamaz"
 * ✓ "Fiyat sıfırdan büyük olmalıdır"
 * ✓ "Stok miktarı negatif olamaz"
 * ✓ "Minimum stok seviyesi negatif olamaz"
 *
 * VALIDATION ANNOTATIONS:
 * @NotBlank: null, "", "   " kabul edilmez
 * @NotNull: null kabul edilmez (ama "" kabul edilir)
 * @DecimalMin: Minimum değer kontrolü
 * @Min: Integer için minimum değer
 * @Size: String uzunluk kontrolü
 *
 * @Data (Lombok):
 * - @Getter + @Setter + @ToString + @EqualsAndHashCode + @RequiredArgsConstructor
 * - Tüm standart metodları otomatik oluşturur
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequest {

    /**
     * Ürün Adı
     * TASK: "Ürün adı boş olamaz"
     *
     * @NotBlank: Boşluk, null veya empty string kontrolü
     * @Size: Minimum 2, maksimum 200 karakter
     */
    @NotBlank(message = "Ürün adı boş olamaz")
    @Size(min = 2, max = 200, message = "Ürün adı 2-200 karakter arasında olmalıdır")
    private String name;

    /**
     * Ürün Açıklaması
     * Opsiyonel alan (validation yok)
     */
    @Size(max = 1000, message = "Açıklama maksimum 1000 karakter olabilir")
    private String description;

    /**
     * Ürün Kategorisi
     * TASK: ELECTRONICS, FOOD, CLOTHING, BOOK, OTHER
     *
     * @NotNull: Enum olduğu için @NotBlank değil @NotNull kullanıyoruz
     */
    @NotNull(message = "Kategori boş olamaz")
    private ProductCategory category;

    /**
     * Ürün Fiyatı
     * TASK: "Fiyat sıfırdan büyük olmalıdır"
     * TASK: "price alanı için BigDecimal kullanılmalıdır"
     *
     * @NotNull: Null olamaz
     * @DecimalMin: Minimum 0.01 (0'dan büyük)
     * @Digits: Toplam 19 basamak, virgülden sonra 2 basamak
     *
     * NEDEN "0.01"?
     * - Fiyat 0 olamaz (ücretsiz ürün varsa farklı mantık gerekir)
     * - En düşük fiyat 0.01 TL
     */
    @NotNull(message = "Fiyat boş olamaz")
    @DecimalMin(value = "0.01", message = "Fiyat sıfırdan büyük olmalıdır")
    @Digits(integer = 17, fraction = 2, message = "Fiyat formatı hatalı (max 17 basamak, 2 ondalık)")
    private BigDecimal price;

    /**
     * Stok Miktarı
     * TASK: "Stok miktarı negatif olamaz"
     *
     * @NotNull: Null olamaz
     * @Min(0): Minimum 0 (negatif olamaz)
     */
    @NotNull(message = "Stok miktarı boş olamaz")
    @Min(value = 0, message = "Stok miktarı negatif olamaz")
    private Integer stockQuantity;

    /**
     * Minimum Stok Seviyesi
     * TASK: "Minimum stok seviyesi negatif olamaz"
     *
     * Düşük stok uyarısı için kullanılır
     * stockQuantity <= minimumStockLevel ise "Low Stock"
     */
    @NotNull(message = "Minimum stok seviyesi boş olamaz")
    @Min(value = 0, message = "Minimum stok seviyesi negatif olamaz")
    private Integer minimumStockLevel;
}