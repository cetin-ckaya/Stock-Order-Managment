package com.hibernate.stockordermanagment.controller;

import com.hibernate.stockordermanagment.dto.request.CreateProductRequest;
import com.hibernate.stockordermanagment.dto.request.UpdateProductRequest;
import com.hibernate.stockordermanagment.dto.response.ProductResponse;
import com.hibernate.stockordermanagment.enums.ProductCategory;
import com.hibernate.stockordermanagment.enums.ProductStatus;
import com.hibernate.stockordermanagment.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product Controller
 *
 * TASK GEREKSİNİMİ:
 * ✓ POST /api/v1/products - Ürün oluşturma
 * ✓ GET /api/v1/products - Ürün listeleme (filtreli, sayfalı)
 * ✓ GET /api/v1/products/{id} - Tekil ürün getirme
 * ✓ PUT /api/v1/products/{id} - Ürün güncelleme
 * ✓ DELETE /api/v1/products/{id} - Ürün silme
 * ✓ GET /api/v1/products/low-stock - Düşük stoklu ürünler
 *
 * TASK KURALI: "Controller içerisinde iş mantığı yazılmamalıdır"
 * Controller sadece:
 * 1. HTTP isteğini alır
 * 2. Parametreleri parse eder
 * 3. Service'e iletir
 * 4. Response döner
 *
 * @RestController: @Controller + @ResponseBody
 * - JSON response otomatik dönüştürülür
 *
 * @RequestMapping: Tüm endpoint'lerin base path'i
 *
 * @Tag: Swagger dokümantasyonu için
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "Ürün yönetimi için API endpoint'leri")
public class ProductController {

    /**
     * Interface inject ediyoruz, implementasyon değil
     * TASK: "Interface ve abstraction" kullanımı
     * Bu sayede implementasyonu değiştirirsek Controller etkilenmez
     */
    private final ProductService productService;

    /**
     * Ürün Oluşturma
     * TASK: POST /api/v1/products
     *
     * @Valid: DTO üzerindeki validation annotation'larını tetikler
     * - @NotBlank, @NotNull, @DecimalMin vb.
     * - Hata olursa GlobalExceptionHandler yakalar
     *
     * ResponseEntity.status(HttpStatus.CREATED):
     * - 201 Created döner (200 OK değil)
     * - REST standartlarına uygun
     */
    @PostMapping
    @Operation(
            summary = "Yeni ürün oluştur",
            description = "Sisteme yeni bir ürün ekler. Aynı isimde aktif ürün varsa hata döner."
    )
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {

        log.info("POST /api/v1/products - Creating product: {}", request.getName());

        ProductResponse response = productService.createProduct(request);

        return ResponseEntity
                .status(HttpStatus.CREATED) // 201
                .body(response);
    }

    /**
     * Tekil Ürün Getirme
     * TASK: GET /api/v1/products/{id}
     *
     * @PathVariable: URL'deki {id} değerini alır
     * Örnek: GET /api/v1/products/1 → id = 1
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "ID ile ürün getir",
            description = "Belirtilen ID'ye sahip ürünü getirir. Bulunamazsa 404 döner."
    )
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Ürün ID'si") @PathVariable Long id) {

        log.info("GET /api/v1/products/{}", id);

        ProductResponse response = productService.getProductById(id);

        return ResponseEntity.ok(response); // 200 OK
    }

    /**
     * Filtreleme ve Sayfalama ile Ürün Listeleme
     *
     * TASK: GET /api/v1/products?category=ELECTRONICS&minPrice=500&maxPrice=5000&page=0&size=20
     *
     * TASK GEREKSİNİMİ:
     * ✓ category filtresi
     * ✓ status filtresi
     * ✓ minPrice/maxPrice filtresi
     * ✓ search filtresi
     * ✓ Sayfalama (page, size)
     * ✓ Sıralama (sort)
     *
     * @RequestParam(required = false): Opsiyonel parametre
     * defaultValue: Parametre gönderilmezse kullanılacak değer
     *
     * ÖRNEK İSTEKLER:
     * GET /api/v1/products
     * GET /api/v1/products?category=ELECTRONICS
     * GET /api/v1/products?minPrice=100&maxPrice=500&page=0&size=10
     * GET /api/v1/products?search=kulaklık&sort=price,desc
     */
    @GetMapping
    @Operation(
            summary = "Ürünleri listele",
            description = "Filtreleme ve sayfalama ile ürün listesi getirir"
    )
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @Parameter(description = "Kategori filtresi")
            @RequestParam(required = false) ProductCategory category,

            @Parameter(description = "Durum filtresi")
            @RequestParam(required = false) ProductStatus status,

            @Parameter(description = "Minimum fiyat")
            @RequestParam(required = false) BigDecimal minPrice,

            @Parameter(description = "Maximum fiyat")
            @RequestParam(required = false) BigDecimal maxPrice,

            @Parameter(description = "İsim araması")
            @RequestParam(required = false) String search,

            @Parameter(description = "Sayfa numarası (0'dan başlar)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Sayfa boyutu")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sıralama (örn: price,desc veya name,asc)")
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        log.info("GET /api/v1/products - category: {}, status: {}, page: {}, size: {}",
                category, status, page, size);

        // Sort parametresini parse et
        // "price,desc" → Sort.by(Direction.DESC, "price")
        Pageable pageable = buildPageable(page, size, sort);

        Page<ProductResponse> response = productService.getProducts(
                category, status, minPrice, maxPrice, search, pageable
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Ürün Güncelleme
     * TASK: PUT /api/v1/products/{id}
     *
     * Partial Update: Sadece gönderilen alanlar güncellenir
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Ürün güncelle",
            description = "Belirtilen ID'ye sahip ürünü günceller. Sadece gönderilen alanlar güncellenir."
    )
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Ürün ID'si") @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {

        log.info("PUT /api/v1/products/{}", id);

        ProductResponse response = productService.updateProduct(id, request);

        return ResponseEntity.ok(response);
    }

    /**
     * Ürün Silme
     * TASK: DELETE /api/v1/products/{id}
     *
     * Soft Delete: Fiziksel silme yerine PASSIVE yapıyoruz
     * ResponseEntity.noContent(): 204 No Content döner
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Ürün sil",
            description = "Belirtilen ID'ye sahip ürünü pasif duruma getirir (soft delete)"
    )
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Ürün ID'si") @PathVariable Long id) {

        log.info("DELETE /api/v1/products/{}", id);

        productService.deleteProduct(id);

        return ResponseEntity.noContent().build(); // 204
    }

    /**
     * Düşük Stoklu Ürünler
     * TASK: GET /api/v1/products/low-stock
     * TASK: stockQuantity <= minimumStockLevel olan ürünler
     *
     * ÖNEMLİ: Bu mapping "/low-stock" olduğu için
     * "/{id}" mapping'inden ÖNCE tanımlanmalıdır.
     * Aksi halde Spring "low-stock" kelimesini ID olarak algılar!
     */
    @GetMapping("/low-stock")
    @Operation(
            summary = "Düşük stoklu ürünler",
            description = "Stok miktarı minimum stok seviyesinin altında olan ürünleri listeler"
    )
    public ResponseEntity<List<ProductResponse>> getLowStockProducts() {

        log.info("GET /api/v1/products/low-stock");

        List<ProductResponse> response = productService.getLowStockProducts();

        return ResponseEntity.ok(response);
    }

    /**
     * En Pahalı 5 Ürün
     * TASK: "En pahalı ilk beş ürünün bulunması" - Stream API ile
     */
    @GetMapping("/top-expensive")
    @Operation(
            summary = "En pahalı 5 ürün",
            description = "Fiyata göre en pahalı 5 ürünü listeler"
    )
    public ResponseEntity<List<ProductResponse>> getTop5ExpensiveProducts() {

        log.info("GET /api/v1/products/top-expensive");

        List<ProductResponse> response = productService.getTop5ExpensiveProducts();

        return ResponseEntity.ok(response);
    }

    // ==================== PRIVATE HELPER METODLAR ====================

    /**
     * Pageable Oluşturma
     *
     * TASK: "Sayfalama kullanılmalıdır"
     * TASK: "Ürünler fiyat, stok miktarı, oluşturulma tarihi ve
     *        ürün adına göre sıralanabilmelidir"
     *
     * sort parametresi formatı: "field,direction"
     * Örnekler:
     * - "price,desc" → Fiyata göre büyükten küçüğe
     * - "name,asc" → İsme göre A'dan Z'ye
     * - "createdAt,desc" → En yeni önce
     * - "stockQuantity,asc" → En az stok önce
     *
     * @param page Sayfa numarası
     * @param size Sayfa boyutu
     * @param sort Sıralama parametresi
     * @return Pageable nesnesi
     */
    private Pageable buildPageable(int page, int size, String sort) {
        try {
            // "price,desc" → ["price", "desc"]
            String[] sortParams = sort.split(",");
            String sortField = sortParams[0].trim();
            Sort.Direction direction = sortParams.length > 1
                    && sortParams[1].trim().equalsIgnoreCase("asc")
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;

            return PageRequest.of(page, size, Sort.by(direction, sortField));
        } catch (Exception e) {
            // Parse hatası olursa varsayılan sıralama
            log.warn("Invalid sort parameter: {}, using default", sort);
            return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
    }
}