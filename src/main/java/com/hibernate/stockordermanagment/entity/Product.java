package com.hibernate.stockordermanagment.entity;

import com.hibernate.stockordermanagment.enums.ProductCategory;
import com.hibernate.stockordermanagment.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ürün Entity Sınıfı
 *
 * TASK GEREKSİNİMLERİ:
 * ✓ id, name, description, category, price, stockQuantity, minimumStockLevel, status, createdAt, updatedAt
 * ✓ price alanı için BigDecimal kullanılmalıdır
 * ✓ Enum kullanımı (ProductStatus, ProductCategory)
 * ✓ Concurrency için @Version (Optimistic Locking)
 *
 * JPA ANNOTATIONS:
 * @Entity: Bu sınıfın bir veritabanı tablosunu temsil ettiğini belirtir
 * @Table: Tablo adını ve indexleri tanımlar
 * @Id: Primary key alanını belirtir
 * @GeneratedValue: Primary key'in otomatik artan olduğunu belirtir
 * @Column: Kolon özelliklerini tanımlar (nullable, length, precision vs.)
 * @Enumerated: Enum tipindeki alanların veritabanında nasıl saklanacağını belirtir
 * @Version: Optimistic locking için kullanılır (concurrency kontrolü)
 * @CreationTimestamp: Hibernate otomatik oluşturma zamanını atar
 * @UpdateTimestamp: Hibernate otomatik güncelleme zamanını atar
 * @PrePersist: Insert işleminden önce çalışır
 * @PreUpdate: Update işleminden önce çalışır
 *
 * LOMBOK ANNOTATIONS:
 * @Getter/@Setter: Getter/Setter metodları otomatik oluşturur
 * @NoArgsConstructor: Parametresiz constructor oluşturur (JPA için gerekli)
 * @AllArgsConstructor: Tüm parametreli constructor oluşturur
 * @Builder: Builder pattern implementasyonu (test yazmayı kolaylaştırır)
 */
@Entity
@Table(name = "products",indexes = {
        // NEDEN INDEX?
        // Sık sorgulanan alanlara index ekleyerek sorgu performansını artırıyoruz
        // Örnek: WHERE name = 'Kablosuz Kulaklık' sorgusu çok hızlı çalışır
        @Index(name = "idx_product_name",columnList = "name"),
        @Index(name = "idx_product_category",columnList = "category"),
        @Index(name = "idx_product_status",columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor // JPA için boş constructor şart
@AllArgsConstructor
@Builder
public class Product {
    /**
     * Primary Key
     * GenerationType.IDENTITY: PostgreSQL'de SERIAL tipini kullanır
     * Her yeni kayıtta otomatik artar (1, 2, 3, ...)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Ürün Adı
     * TASK KURALI: "Ürün adı boş olamaz"
     * nullable = false: Database seviyesinde NOT NULL constraint
     * length = 200: VARCHAR(200)
     */
    @Column(nullable = false,length = 200)
    private String name;

    /**
     * Ürün Açıklaması
     * Opsiyonel alan (nullable = true, default)
     */
    @Column(length = 200)
    private String description;

    /**
     * Ürün Kategorisi
     * @Enumerated(EnumType.STRING): Database'de enum değerini string olarak saklar
     *
     * NEDEN STRING?
     * - EnumType.ORDINAL: 0,1,2 şeklinde saklar (enum sırasını değiştirirseniz veri bozulur)
     * - EnumType.STRING: "ELECTRONICS", "FOOD" şeklinde saklar (daha güvenli)
     *
     * TASK GEREKSİNİMİ: Kategori bazlı filtreleme yapılacak
     */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 50)
    private ProductCategory category;

    /**
     * Ürün Fiyatı
     * TASK GEREKSİNİMİ: "price alanı için BigDecimal kullanılmalıdır"
     *
     * NEDEN BigDecimal?
     * - double/float: Ondalık sayılarda hassasiyet kaybı olur
     *   Örnek: 0.1 + 0.2 = 0.30000000000000004 (YANLIŞ!)
     * - BigDecimal: Tam hassasiyet, para hesaplamalarında ZORUNLU
     *   Örnek: 0.1 + 0.2 = 0.3 (DOĞRU)
     *
     * precision = 19: Toplam 19 basamak
     * scale = 2: Virgülden sonra 2 basamak
     * Örnek: 99999999999999999.99 maks değer
     */
    @Column(nullable = false,precision = 19,scale = 2)
    private BigDecimal price;

    /**
     * Stok Miktarı
     * TASK GEREKSİNİMİ: "Stok miktarı negatif olamaz"
     *
     * CONCURRENCY: Aynı anda birden fazla sipariş gelirse bu değer @Version ile korunur
     * TASK: "Aynı anda sipariş verme problemi" - Bu alan kritik!
     */
    @Column(nullable = false)
    private Integer stockQuantity;

    /**
     * Minimum Stok Seviyesi
     * TASK GEREKSİNİMİ: "minimumStockLevel" - Stok uyarısı için kullanılır
     *
     * Kullanım: GET /api/v1/products/low-stock endpoint'inde
     * stockQuantity <= minimumStockLevel olan ürünler döner
     */
    @Column(nullable = false)
    private Integer minimumStockLevel;

    /**
     * Ürün Durumu
     * TASK: Ürün aktif, pasif veya stokta yok olabilir
     *
     * OTOMATİK GÜNCELLEME:
     * - stockQuantity = 0 olursa → OUT_OF_STOCK
     * - stockQuantity > 0 olursa → ACTIVE
     * Bu mantık @PrePersist ve @PreUpdate'de implement edildi
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    /**
     * Version - Optimistic Locking
     * TASK GEREKSİNİMİ: "Concurrency ve race condition"
     *
     * NASIL ÇALIŞIR?
     * 1. Ürün okunur: version = 1
     * 2. İki kullanıcı aynı anda günceller
     * 3. İlk kullanıcı kaydeder: version = 2 olur
     * 4. İkinci kullanıcı kaydetmeye çalışır: version hala 1 (eski)
     * 5. OptimisticLockException fırlatılır
     *
     * TASK SORUSU: "@Version alanı ne işe yarar?" - Cevabı bu!
     *
     * AVANTAJLAR:
     * - Database lock'a gerek yok
     * - Performanslı
     * - Ölçeklenebilir
     *
     * DEZAVANTAJLAR:
     * - Retry mekanizması gerekebilir
     * - Çakışma durumunda exception
     */
    @Version
    private Long version;

    /**
     * Oluşturulma Zamanı
     * @CreationTimestamp: Hibernate insert sırasında otomatik atar
     * updatable = false: Update işlemlerinde değişmez
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Güncellenme Zamanı
     * @UpdateTimestamp: Hibernate update sırasında otomatik günceller
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Insert öncesi çalışır
     * TASK: Varsayılan değerleri set et, validasyonları yap
     */
    @PrePersist
    private void prePersist() {
        // Eğer status set edilmemişse varsayılan ACTIVE yap
        if (status == null) {
            status = ProductStatus.ACTIVE;
        }
        // Stok durumunu güncelle
        updateStockStatus();
    }

    /**
     * Update öncesi çalışır
     * Her güncellemede stok durumunu kontrol et
     */
    @PreUpdate
    private void preUpdate() {
        updateStockStatus();
    }

    /**
     * Stok Durumu Güncelleme
     * TASK MANTIK: Stok bitince otomatik OUT_OF_STOCK yap
     *
     * Business Logic:
     * - Stok = 0 → OUT_OF_STOCK
     * - Stok > 0 ve durum OUT_OF_STOCK → ACTIVE'e çevir
     */
    private void updateStockStatus() {
        if (stockQuantity != null && stockQuantity == 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        } else if (this.status == ProductStatus.OUT_OF_STOCK && stockQuantity > 0) {
            // Stok eklendiyse tekrar aktif yap
            this.status = ProductStatus.ACTIVE;
        }
    }

    /**
     * Stok Azaltma Metodu
     * TASK: Sipariş oluşturulurken stok azaltılacak
     *
     * @param quantity Azaltılacak miktar
     * @throws IllegalArgumentException Yetersiz stok durumunda
     *
     * NEDEN BURADA?
     * - Encapsulation: Stok mantığı Product entity içinde
     * - Business Logic: Negatif stok olmasını engeller
     * - Single Responsibility: Stok yönetimi Product'ın sorumluluğu
     */
    public void decreaseStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new IllegalArgumentException("Yetersiz stok");
        }
        this.stockQuantity -= quantity;
        updateStockStatus(); // Stok 0 olduysa OUT_OF_STOCK yap
    }

    /**
     * Stok Arttırma Metodu
     * TASK: Sipariş iptal edildiğinde stok geri eklenecek
     *
     * @param quantity Eklenecek miktar
     */
    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
        updateStockStatus(); // Stok eklendiyse ACTIVE yap
    }

    /**
     * Düşük Stok Kontrolü
     * TASK: "Stok seviyesi düşük ürünler" endpoint'i için
     *
     * @return true ise stok minimum seviyenin altında veya eşit
     */
    public boolean isLowStock() {
        return stockQuantity <= minimumStockLevel;
    }
}
