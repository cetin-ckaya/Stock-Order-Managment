package com.hibernate.stockordermanagment.repository;

import com.hibernate.stockordermanagment.entity.Product;
import com.hibernate.stockordermanagment.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Product Repository
 *
 * TASK GEREKSİNİMİ: "Spring Data JPA"
 *
 * JpaRepository<Product, Long>:
 * - Product: Entity tipi
 * - Long: Primary key tipi
 * - save(), findById(), findAll(), delete() gibi metodlar otomatik gelir
 *
 * JpaSpecificationExecutor<Product>:
 * - Dinamik filtreleme için gerekli
 * - category, status, minPrice, maxPrice gibi filtreler için kullanacağız
 * - findAll(Specification, Pageable) metodunu sağlar
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    /**
     * TASK KURALI: "Aynı isimle ikinci bir aktif ürün oluşturulmamalıdır"
     *
     * Ürün oluştururken bu metodu kullanacağız.
     * Eğer true dönerse DuplicateProductException fırlatacağız.
     *
     * Spring Data JPA metodun adından otomatik SQL üretir:
     * SELECT COUNT(*) > 0 FROM products WHERE name = ? AND status = ?
     */
    boolean existsByNameAndStatus(String name, ProductStatus status);

    /**
     * TASK GEREKSİNİMİ: "Stok seviyesi düşük ürünlerin listelenmesi"
     * GET /api/v1/products/low-stock
     *
     * stockQuantity <= minimumStockLevel olan ürünleri getirir
     *
     * JPQL Kullanımı:
     * - "p" Product entity'nin alias'ı
     * - p.stockQuantity: Entity field adı (database kolon adı değil!)
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.minimumStockLevel")
    List<Product> findLowStockProducts();

    /**
     * Aktif ürünleri kategoriye göre listeler
     * Stream API ile birlikte kullanılacak
     *
     * TASK: "Belirli bir kategorideki ürünlerin filtrelenmesi"
     */
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' ORDER BY p.price DESC")
    List<Product> findTop5ByOrderByPriceDesc();

    /**
     * İsim ve duruma göre ürün ara
     * Duplicate kontrolü için kullanılır
     */
    Optional<Product> findByNameAndStatus(String name, ProductStatus status);
}