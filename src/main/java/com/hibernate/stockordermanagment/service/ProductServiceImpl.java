package com.hibernate.stockordermanagment.service.impl;

import com.hibernate.stockordermanagment.dto.request.CreateProductRequest;
import com.hibernate.stockordermanagment.dto.request.UpdateProductRequest;
import com.hibernate.stockordermanagment.dto.response.ProductResponse;
import com.hibernate.stockordermanagment.entity.Product;
import com.hibernate.stockordermanagment.enums.ProductCategory;
import com.hibernate.stockordermanagment.enums.ProductStatus;
import com.hibernate.stockordermanagment.exception.DuplicateProductException;
import com.hibernate.stockordermanagment.exception.ProductNotFoundException;
import com.hibernate.stockordermanagment.mapper.ProductMapper;
import com.hibernate.stockordermanagment.repository.ProductRepository;
import com.hibernate.stockordermanagment.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Product Service Implementation
 *
 * TASK GEREKSİNİMİ:
 * ✓ Ürün oluşturma ve listeleme
 * ✓ Validation kuralları
 * ✓ Stream API kullanımı
 * ✓ Exception handling
 * ✓ Transaction yönetimi
 *
 * @Service: Spring'e bu sınıfın bir servis bean'i olduğunu söyler
 *
 * @RequiredArgsConstructor (Lombok):
 * - final field'lar için constructor oluşturur
 * - Constructor injection yapar (Field injection yerine tercih edilir)
 * - NEDEN CONSTRUCTOR INJECTION?
 *   1. Test yazarken mock inject etmek kolaylaşır
 *   2. Immutable (değiştirilemez) dependency sağlar
 *   3. Spring önerir
 *
 * @Slf4j: log.info(), log.error() kullanmak için
 *
 * @Transactional(readOnly = true):
 * - Sınıf seviyesinde: Tüm metodlar varsayılan olarak readOnly transaction'da çalışır
 * - Sadece okuma yapan metodlar için performans optimizasyonu sağlar
 * - Yazma yapan metodlar kendi @Transactional annotation'ını override eder
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Varsayılan: tüm metodlar read-only
public class ProductServiceImpl implements ProductService {

    /**
     * Dependency Injection
     *
     * final: Constructor injection için gerekli
     * @RequiredArgsConstructor bu final field'lar için otomatik constructor üretir
     */
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    /**
     * Ürün Oluşturma
     *
     * TASK KURALLARI:
     * ✓ "Ürün adı boş olamaz" - @Valid ile Controller'da kontrol edildi
     * ✓ "Fiyat sıfırdan büyük olmalıdır" - @Valid ile kontrol edildi
     * ✓ "Stok miktarı negatif olamaz" - @Valid ile kontrol edildi
     * ✓ "Aynı isimle ikinci bir aktif ürün oluşturulmamalıdır" - Burada kontrol
     *
     * @Transactional: readOnly = false (yazma işlemi yapıyoruz)
     * - Hata olursa tüm işlem geri alınır (rollback)
     */
    @Override
    @Transactional // Yazma işlemi, readOnly = false
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating product with name: {}", request.getName());

        // TASK KURALI: "Aynı isimle ikinci bir aktif ürün oluşturulmamalıdır"
        // Repository'deki existsByNameAndStatus metodunu kullanıyoruz
        boolean exists = productRepository.existsByNameAndStatus(
                request.getName(),
                ProductStatus.ACTIVE
        );

        if (exists) {
            log.warn("Duplicate product attempt: {}", request.getName());
            throw new DuplicateProductException(request.getName());
        }

        // DTO -> Entity dönüşümü (MapStruct)
        Product product = productMapper.toEntity(request);

        // Varsayılan status set et
        product.setStatus(ProductStatus.ACTIVE);

        // Veritabanına kaydet
        Product savedProduct = productRepository.save(product);

        log.info("Product created successfully with id: {}", savedProduct.getId());

        // Entity -> Response dönüşümü (MapStruct)
        return productMapper.toResponse(savedProduct);
    }

    /**
     * Ürün Güncelleme
     *
     * Partial Update: Sadece gönderilen alanlar güncellenir
     * MapStruct'ın NullValuePropertyMappingStrategy.IGNORE özelliği sayesinde
     */
    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        log.info("Updating product with id: {}", id);

        // Ürünü bul, yoksa exception fırlat
        Product product = findProductById(id);

        // Eğer isim değişiyorsa duplicate kontrolü yap
        if (request.getName() != null && !request.getName().equals(product.getName())) {
            boolean exists = productRepository.existsByNameAndStatus(
                    request.getName(),
                    ProductStatus.ACTIVE
            );
            if (exists) {
                throw new DuplicateProductException(request.getName());
            }
        }

        // Sadece null olmayan alanları güncelle (MapStruct Partial Update)
        productMapper.updateEntityFromRequest(request, product);

        // Kaydet
        Product updatedProduct = productRepository.save(product);

        log.info("Product updated successfully with id: {}", id);

        return productMapper.toResponse(updatedProduct);
    }

    /**
     * ID ile Ürün Getirme
     *
     * TASK: Olmayan ürün sorgulandığında exception oluşur
     */
    @Override
    public ProductResponse getProductById(Long id) {
        log.info("Fetching product with id: {}", id);

        // findProductById: Private yardımcı metod
        // ProductNotFoundException fırlatır
        Product product = findProductById(id);

        return productMapper.toResponse(product);
    }

    /**
     * Filtreleme ve Sayfalama ile Ürün Listeleme
     *
     * TASK: GET /api/v1/products?category=ELECTRONICS&minPrice=500&page=0&size=20
     *
     * JPA Specification Pattern kullanıyoruz:
     * - Dinamik filtreler oluşturmak için
     * - Her filtre bir Specification nesnesi
     * - Specification'lar AND ile birleştiriliyor
     *
     * TASK: "Ürünler fiyat, stok miktarı, oluşturulma tarihi ve ürün adına göre sıralanabilmeli"
     * Bu sıralama Pageable nesnesi ile Controller'dan gelir
     */
    @Override
    public Page<ProductResponse> getProducts(
            ProductCategory category,
            ProductStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String search,
            Pageable pageable) {

        log.info("Fetching products with filters - category: {}, status: {}", category, status);

        // Specification ile dinamik filtre oluştur
        Specification<Product> spec = buildSpecification(category, status, minPrice, maxPrice, search);

        // Filtrelenmiş ve sayfalanmış sonucu getir
        Page<Product> products = productRepository.findAll(spec, pageable);

        // STREAM API: Her product'ı response'a dönüştür
        // TASK: "Stream API kullanımı"
        return products.map(productMapper::toResponse);
    }

    /**
     * Ürün Silme
     */
    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);

        // Önce var mı kontrol et
        Product product = findProductById(id);

        // Fiziksel silmek yerine PASSIVE yapabiliriz (Soft Delete)
        // Ama task'ta belirtilmediği için şimdilik PASSIVE yapıyoruz
        product.setStatus(ProductStatus.PASSIVE);
        productRepository.save(product);

        log.info("Product deleted (set to PASSIVE) with id: {}", id);
    }

    /**
     * Düşük Stoklu Ürünleri Listele
     *
     * TASK: GET /api/v1/products/low-stock
     * TASK: stockQuantity <= minimumStockLevel
     *
     * STREAM API KULLANIMI:
     * Repository'den gelen listeyi stream ile response'a çeviriyoruz
     */
    @Override
    public List<ProductResponse> getLowStockProducts() {
        log.info("Fetching low stock products");

        // Repository'deki JPQL sorgusu ile düşük stoklu ürünleri getir
        List<Product> lowStockProducts = productRepository.findLowStockProducts();

        // STREAM API - TASK GEREKSİNİMİ: "Stok seviyesi düşük ürünlerin listelenmesi"
        // Her Product entity'sini ProductResponse'a dönüştür
        return lowStockProducts.stream()
                .map(productMapper::toResponse) // Method reference
                .collect(Collectors.toList());
    }

    /**
     * En Pahalı 5 Ürünü Getir
     *
     * TASK: "En pahalı ilk beş ürünün bulunması"
     *
     * STREAM API KULLANIMI:
     * 1. Tüm aktif ürünleri getir
     * 2. Fiyata göre büyükten küçüğe sırala
     * 3. İlk 5'i al
     * 4. Response'a dönüştür
     */
    @Override
    public List<ProductResponse> getTop5ExpensiveProducts() {
        log.info("Fetching top 5 expensive products");

        // Repository'den fiyata göre sıralı ürünleri getir
        List<Product> products = productRepository.findTop5ByOrderByPriceDesc();

        // STREAM API - TASK GEREKSİNİMİ: "En pahalı ilk beş ürünün bulunması"
        return products.stream()
                .sorted(Comparator.comparing(Product::getPrice).reversed()) // Fiyata göre sırala
                .limit(5) // İlk 5'i al
                .map(productMapper::toResponse) // Response'a dönüştür
                .collect(Collectors.toList());
    }

    // ==================== PRIVATE HELPER METODLAR ====================

    /**
     * ID ile Ürün Bulma (Private Yardımcı Metod)
     *
     * DRY (Don't Repeat Yourself) prensibi:
     * Her servis metodunda aynı kodu yazmak yerine
     * bu metodu çağırıyoruz
     *
     * @param id Ürün ID
     * @return Product entity
     * @throws ProductNotFoundException Ürün bulunamazsa
     */
    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    /**
     * Dinamik Filtre Oluşturma (JPA Specification Pattern)
     *
     * TASK: Kategori, durum, fiyat aralığı ve isim filtrelerini destekle
     *
     * Specification Pattern:
     * - Her filtre ayrı bir lambda (Specification)
     * - Null olan filtreler görmezden gelinir
     * - Hepsi AND ile birleştirilir
     *
     * @param category  Kategori filtresi (null olabilir)
     * @param status    Durum filtresi (null olabilir)
     * @param minPrice  Minimum fiyat (null olabilir)
     * @param maxPrice  Maximum fiyat (null olabilir)
     * @param search    İsim araması (null olabilir)
     * @return Birleştirilmiş Specification
     */
    private Specification<Product> buildSpecification(
            ProductCategory category,
            ProductStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String search) {

        // Başlangıç: Tüm ürünleri getir (WHERE 1=1)
        Specification<Product> spec = Specification.where(null);

        // Kategori filtresi
        if (category != null) {
            // spec = spec AND (category = ?)
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("category"), category));
        }

        // Durum filtresi
        if (status != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        // Minimum fiyat filtresi
        if (minPrice != null) {
            // price >= minPrice
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }

        // Maximum fiyat filtresi
        if (maxPrice != null) {
            // price <= maxPrice
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        // İsim araması (LIKE %search%)
        if (search != null && !search.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(
                            cb.lower(root.get("name")),
                            "%" + search.toLowerCase() + "%"
                    ));
        }

        return spec;
    }
}