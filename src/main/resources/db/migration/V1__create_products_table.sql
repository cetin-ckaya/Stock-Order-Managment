-- ====================================================================
-- FLYWAY MIGRATION: V1
-- TASK GEREKSİNİMİ: "Migration dosyaları" (Flyway veya Liquibase)
--
-- NEDEN FLYWAY?
-- 1. Versiyon Kontrolü: Her değişiklik numaralandırılır (V1, V2, V3...)
-- 2. Tekrarlanabilirlik: Herkes aynı veritabanı şemasına sahip olur
-- 3. Otomatik Çalışma: Uygulama başlarken otomatik migration yapar
-- 4. Rollback Güvenliği: Hangi değişikliğin ne zaman yapıldığı izlenebilir
-- 5. Takım Çalışması: SQL script'leri git ile versiyonlanır
--
-- ddl-auto: validate kullanıyoruz (create/update DEĞİL)
-- Çünkü production'da Hibernate'in şemayı otomatik değiştirmesi TEHLİKELİDİR
-- Flyway ile kontrollü ve öngörülebilir migration yapıyoruz
-- ====================================================================

-- ====================================================================
-- PRODUCTS TABLOSU
-- Entity: com.hibernate.stockordermanagment.entity.Product
-- ====================================================================
CREATE TABLE products (
    -- Primary Key
    -- GenerationType.IDENTITY karşılığı: BIGSERIAL (PostgreSQL'de auto-increment)
                          id BIGSERIAL PRIMARY KEY,

    -- TASK KURALI: "Ürün adı boş olamaz"
    -- NOT NULL constraint database seviyesinde de garanti eder
                          name VARCHAR(200) NOT NULL,

    -- Opsiyonel alan, NULL olabilir
                          description VARCHAR(1000),

    -- TASK: ProductCategory enum'ı (ELECTRONICS, FOOD, CLOTHING, BOOK, OTHER)
    -- EnumType.STRING kullandığımız için VARCHAR olarak saklıyoruz
                          category VARCHAR(50) NOT NULL,

    -- TASK GEREKSİNİMİ: "price alanı için BigDecimal kullanılmalıdır"
    -- NUMERIC(19,2): PostgreSQL'de BigDecimal karşılığı
    -- 19 basamak toplam, 2 basamak virgülden sonra
    -- TASK KURALI: "Fiyat sıfırdan büyük olmalıdır" - CHECK constraint ile garanti
                          price NUMERIC(19, 2) NOT NULL CHECK (price > 0),

    -- TASK KURALI: "Stok miktarı negatif olamaz" - CHECK constraint
                          stock_quantity INTEGER NOT NULL CHECK (stock_quantity >= 0),

    -- TASK KURALI: "Minimum stok seviyesi negatif olamaz" - CHECK constraint
                          minimum_stock_level INTEGER NOT NULL CHECK (minimum_stock_level >= 0),

    -- TASK: ProductStatus enum'ı (ACTIVE, PASSIVE, OUT_OF_STOCK)
                          status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    -- TASK GEREKSİNİMİ: "Concurrency ve race condition"
    -- @Version alanının veritabanı karşılığı
    -- Optimistic Locking için kullanılır
    -- Her UPDATE işleminde otomatik +1 artar
                          version BIGINT NOT NULL DEFAULT 0,

    -- Otomatik zaman damgaları
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ====================================================================
-- INDEXLER
-- Entity'deki @Table(indexes = {...}) tanımlarının veritabanı karşılığı
--
-- NEDEN INDEX?
-- - Sık sorgulanan kolonlarda arama hızını artırır
-- - WHERE, ORDER BY, JOIN işlemlerinde performans sağlar
-- - Index olmadan tam tablo taraması (full table scan) yapılır
-- ====================================================================

-- İsim ile arama yaparken kullanılır (search filtresi için)
CREATE INDEX idx_product_name ON products(name);

-- Kategori filtrelemesi için (GET /api/v1/products?category=ELECTRONICS)
CREATE INDEX idx_product_category ON products(category);

-- Durum filtrelemesi için (GET /api/v1/products?status=ACTIVE)
CREATE INDEX idx_product_status ON products(status);

-- ====================================================================
-- YORUM (COMMENT) EKLEME
-- Veritabanı dokümantasyonu için faydalı (DBA'lar için)
-- ====================================================================
COMMENT ON TABLE products IS 'Ürün bilgilerini tutan ana tablo';
COMMENT ON COLUMN products.version IS 'Optimistic locking için kullanılan versiyon numarası';
COMMENT ON COLUMN products.status IS 'ACTIVE, PASSIVE, OUT_OF_STOCK değerlerinden biri';