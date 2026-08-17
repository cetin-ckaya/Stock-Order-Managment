# Stok ve Sipariş Yönetim Sistemi

Java 17 ve Spring Boot ile geliştirilmiş, ürün stok takibi ve sipariş yönetimi sağlayan bir REST API projesidir. Eş zamanlı (concurrent) sipariş isteklerinde stok tutarlılığını korur, sipariş oluşturma, iptal etme ve raporlama işlemlerini destekler.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

## İçindekiler

- [Özellikler](#özellikler)
- [Kullanılan Teknolojiler](#kullanılan-teknolojiler)
- [Gereksinimler](#gereksinimler)
- [Kurulum](#kurulum)
- [Çalıştırma](#çalıştırma)
- [API Dokümantasyonu](#api-dokümantasyonu)
- [Proje Yapısı](#proje-yapısı)
- [Endpoint Listesi](#endpoint-listesi)
- [Testleri Çalıştırma](#testleri-çalıştırma)
- [Ortam Değişkenleri](#ortam-değişkenleri)
- [Mimari Kararlar](#mimari-kararlar)
- [Katkıda Bulunma](#katkıda-bulunma)
- [Lisans](#lisans)

## Özellikler

- Ürün oluşturma, listeleme, güncelleme, silme (CRUD)
- Kategori, fiyat aralığı, durum ve isim bazlı filtreleme
- Sayfalama ve sıralama desteği
- Sipariş oluşturma ve otomatik stok düşürme
- Sipariş iptali ve stok iadesi
- Eş zamanlı isteklerde stok tutarlılığı (Pessimistic Locking)
- Transaction yönetimi ile veri bütünlüğü
- Düşük stoklu ürün raporu
- En çok satan ürünler raporu
- Sipariş özet raporu (toplam gelir, ortalama tutar vb.)
- Swagger/OpenAPI ile interaktif API dokümantasyonu
- Flyway ile veritabanı versiyon yönetimi
- Docker Compose ile tek komutla veritabanı kurulumu

## Kullanılan Teknolojiler

| Teknoloji | Versiyon | Amaç |
|-----------|----------|------|
| Java | 17 | Programlama dili |
| Spring Boot | 3.2.0 | Uygulama framework'ü |
| Spring Data JPA | - | Veritabanı erişim katmanı |
| PostgreSQL | 15 | İlişkisel veritabanı |
| Flyway | - | Veritabanı migration yönetimi |
| MapStruct | 1.5.5 | Entity-DTO dönüşümü |
| Lombok | 1.18.30 | Boilerplate kod azaltma |
| Springdoc OpenAPI | 2.3.0 | API dokümantasyonu (Swagger) |
| JUnit 5 | - | Unit ve entegrasyon testleri |
| Mockito | - | Mock tabanlı testler |
| Docker Compose | - | Konteynerleştirme |
| Maven | - | Bağımlılık ve derleme yönetimi |

## Gereksinimler

Projeyi çalıştırmadan önce aşağıdakilerin kurulu olduğundan emin olun:

- JDK 17 veya üzeri
- Docker Desktop
- Maven (opsiyonel, proje içinde Maven Wrapper mevcut)
- Git

## Kurulum

Projeyi bilgisayarınıza klonlayın:

```bash
git clone https://github.com/cetin-ckaya/Stock-Order-Managment.git
cd Stock-Order-Managment
```

Çalıştırma
1. Veritabanını Docker ile Başlatın
```bash

docker-compose up -d
Bu komut ile aşağıdaki servisler ayağa kalkar:
```

Servis	Port	Açıklama
PostgreSQL	5433	Ana veritabanı
pgAdmin	5050	Veritabanı yönetim arayüzü
Container'ların çalıştığını doğrulayın:

```bash

docker ps
```
2. Uygulamayı Başlatın
Maven Wrapper ile:

```bash

./mvnw spring-boot:run
```
Windows için:

```bash

mvnw.cmd spring-boot:run
```
IDE üzerinden:

StockOrderManagmentApplication.java dosyasını çalıştırın.

Uygulama başarıyla başladığında http://localhost:8085 adresinden erişilebilir olacaktır. Flyway migration'ları otomatik olarak çalışır ve veritabanı şeması oluşturulur.

API Dokümantasyonu
Uygulama çalıştıktan sonra Swagger UI üzerinden tüm endpoint'leri interaktif olarak test edebilirsiniz:

```text

http://localhost:8085/swagger-ui.html
```
OpenAPI JSON çıktısı için:

```text

http://localhost:8085/api-docs
```
Proje Yapısı
```text

src/main/java/com/hibernate/stockordermanagment/
├── config/            # Swagger, uygulama yapılandırmaları
├── controller/        # REST controller'lar
├── dto/
│   ├── request/       # İstek DTO'ları
│   └── response/      # Yanıt DTO'ları
├── entity/            # JPA entity sınıfları
├── enums/             # Enum tanımları
├── exception/         # Özel exception'lar ve global handler
├── mapper/            # MapStruct mapper arayüzleri
├── repository/        # Spring Data JPA repository'leri
└── service/
    └── impl/          # Servis implementasyonları

src/main/resources/
├── db/migration/      # Flyway migration dosyaları
└── application.properties

src/test/java/         # Unit ve entegrasyon testleri
```
Endpoint Listesi
Ürün Yönetimi

POST	/api/v1/products	Yeni ürün oluşturur
GET	/api/v1/products	Ürünleri filtreli ve sayfalı listeler
GET	/api/v1/products/{id}	ID ile ürün getirir
PUT	/api/v1/products/{id}	Ürünü günceller
DELETE	/api/v1/products/{id}	Ürünü pasif duruma getirir
GET	/api/v1/products/low-stock	Düşük stoklu ürünleri listeler
GET	/api/v1/products/top-expensive	En pahalı 5 ürünü listeler
Sipariş Yönetimi

POST	/api/v1/orders	Yeni sipariş oluşturur
GET	/api/v1/orders	Tüm siparişleri listeler
GET	/api/v1/orders/{id}	ID ile sipariş getirir
PUT	/api/v1/orders/{id}/cancel	Siparişi iptal eder
Raporlama

GET	/api/v1/reports/top-selling-products?limit=5	En çok satan ürünler
GET	/api/v1/reports/order-summary	Genel sipariş özeti
Örnek İstekler
Ürün Oluşturma:

JSON

POST /api/v1/products
{
  "name": "Kablosuz Kulaklık",
  "description": "Bluetooth destekli kulaklık",
  "category": "ELECTRONICS",
  "price": 1500.00,
  "stockQuantity": 20,
  "minimumStockLevel": 5
}
Sipariş Oluşturma:

JSON

POST /api/v1/orders
{
  "customerName": "Ahmet Yılmaz",
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 3, "quantity": 1 }
  ]
}
Testleri Çalıştırma
Tüm testleri çalıştırmak için (Docker container'ının açık olması gerekir):

```bash

./mvnw test
```
Belirli bir test sınıfını çalıştırmak için:

```bash

./mvnw test -Dtest=OrderServiceTest
```
Proje aşağıdaki test türlerini içerir:

Unit Testler (Mockito ile) — ProductServiceTest, OrderServiceTest
Entegrasyon Testleri — OrderTransactionTest, OrderCancelTest
Concurrency Testi — OrderConcurrencyTest
Ortam Değişkenleri
Veritabanı bağlantı bilgileri src/main/resources/application.properties dosyasında tanımlıdır:

```properties

spring.datasource.url=jdbc:postgresql://localhost:5433/stock_order_db
spring.datasource.username=admin
spring.datasource.password=admin123
server.port=8085
```
Farklı bir ortamda çalıştırmak için bu değerleri kendi ortamınıza göre güncelleyebilir veya application-{profile}.properties dosyaları ile profil bazlı yapılandırma yapabilirsiniz.

Mimari Kararlar
Bu proje ile ilgili teknik tercihlerin (neden PostgreSQL, neden Pessimistic Lock, BigDecimal kullanımı, Stream API örnekleri vb.) detaylı gerekçeleri DOCS.md dosyasında yer almaktadır.

Katkıda Bulunma
Bu repository'yi fork edin
Yeni bir branch oluşturun
```bash

git checkout -b feature/yeni-ozellik
```
Değişikliklerinizi commit edin
```bash

git commit -m "özellik: yeni özellik eklendi"
Branch'inizi push edin
```
```bash

git push origin feature/yeni-ozellik
Bir Pull Request açın
```
Lisans
Bu proje MIT lisansı ile lisanslanmıştır.

İletişim
Çetin Çetinkaya - GitHub

Proje Linki: https://github.com/cetin-ckaya/Stock-Order-Managment