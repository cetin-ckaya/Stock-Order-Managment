package com.hibernate.stockordermanagment.service;

import com.hibernate.stockordermanagment.dto.request.CreateProductRequest;
import com.hibernate.stockordermanagment.dto.request.UpdateProductRequest;
import com.hibernate.stockordermanagment.dto.response.ProductResponse;
import com.hibernate.stockordermanagment.enums.ProductCategory;
import com.hibernate.stockordermanagment.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product Service Interface
 *
 * TASK GEREKSİNİMİ: "Interface ve abstraction"
 *
 * NEDEN INTERFACE KULLANIYORUZ?
 * 1. Loosely Coupled (Gevşek Bağlı) Mimari:
 *    - Controller, implementasyonu değil interface'i bilir
 *    - İmplementasyonu değiştirirsen Controller etkilenmez
 *
 * 2. Test Kolaylığı:
 *    - Mockito ile interface'i mock edebiliriz
 *    - Gerçek veritabanına gerek kalmadan test yazabiliriz
 *
 * 3. Birden Fazla İmplementasyon:
 *    - ProductServiceImpl (Normal)
 *    - CachedProductServiceImpl (Redis Cache ile - Bonus)
 *
 * TASK SORUSU: "Interface kullanmanın avantajı nedir?"
 * Cevap: Yukarıdaki 3 madde!
 */
public interface ProductService {

    /**
     * Yeni ürün oluşturur
     * TASK: POST /api/v1/products
     *
     * İş Kuralları:
     * - Ürün adı boş olamaz (Validation)
     * - Fiyat 0'dan büyük olmalı (Validation)
     * - Stok negatif olamaz (Validation)
     * - Aynı isimle aktif ürün olamaz (DuplicateProductException)
     */
    ProductResponse createProduct(CreateProductRequest request);

    /**
     * Var olan ürünü günceller
     * TASK: PUT /api/v1/products/{id}
     *
     * Partial Update: Sadece gönderilen alanlar güncellenir
     */
    ProductResponse updateProduct(Long id, UpdateProductRequest request);

    /**
     * ID ile ürün getirir
     * TASK: GET /api/v1/products/{id}
     *
     * Bulunamazsa ProductNotFoundException fırlatır
     */
    ProductResponse getProductById(Long id);

    /**
     * Filtreleme ve sayfalama ile ürün listesi
     * TASK: GET /api/v1/products?category=ELECTRONICS&minPrice=500&page=0&size=20
     *
     * Desteklenen Filtreler:
     * - category: Kategori filtresi
     * - status: Durum filtresi
     * - minPrice/maxPrice: Fiyat aralığı
     * - search: İsim araması
     * - pageable: Sayfalama ve sıralama
     */
    Page<ProductResponse> getProducts(
            ProductCategory category,
            ProductStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String search,
            Pageable pageable
    );

    /**
     * Ürün siler
     * TASK: DELETE /api/v1/products/{id}
     */
    void deleteProduct(Long id);

    /**
     * Stok seviyesi düşük ürünleri listeler
     * TASK: GET /api/v1/products/low-stock
     * stockQuantity <= minimumStockLevel olan ürünler
     */
    List<ProductResponse> getLowStockProducts();

    /**
     * En pahalı 5 ürünü getirir
     * TASK: "En pahalı ilk beş ürünün bulunması" - Stream API ile
     */
    List<ProductResponse> getTop5ExpensiveProducts();
}