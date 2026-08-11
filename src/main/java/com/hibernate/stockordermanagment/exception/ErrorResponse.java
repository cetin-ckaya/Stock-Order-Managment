package com.hibernate.stockordermanagment.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Standart Hata Yanıt Modeli
 *
 * TASK GEREKSİNİMİ: "Exception handling" - Tüm hatalar standart formatta dönmeli
 *
 * NEDEN STANDART FORMAT?
 * - Frontend tutarlı hata mesajları alır
 * - Hata logları standardize edilir
 * - API dokümantasyonu kolaylaşır
 *
 * @JsonInclude(JsonInclude.Include.NON_NULL):
 * - null olan field'lar JSON'a dahil edilmez
 * - Gereksiz veri transferi önlenir
 * - Response daha temiz görünür
 *
 * ÖRNEK RESPONSE:
 * {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Product not found with id: 999",
 *   "errorCode": "PRODUCT_NOT_FOUND",
 *   "path": "/api/v1/products/999"
 * }
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // null field'ları gösterme
public class ErrorResponse {

    /**
     * Hatanın oluşma zamanı
     * ISO 8601 formatında: 2024-01-15T10:30:00
     */
    private LocalDateTime timestamp;

    /**
     * HTTP Status Code
     * Örnek: 404, 400, 409, 500
     */
    private int status;

    /**
     * HTTP Status Açıklaması
     * Örnek: "Not Found", "Bad Request", "Conflict"
     */
    private String error;

    /**
     * Detaylı Hata Mesajı
     * Türkçe/İngilizce kullanıcı dostu açıklama
     */
    private String message;

    /**
     * Uygulama Bazlı Hata Kodu
     * Frontend'de switch-case ile özel davranışlar için
     * Örnek: "PRODUCT_NOT_FOUND", "INSUFFICIENT_STOCK"
     */
    private String errorCode;

    /**
     * İsteğin Yapıldığı Path
     * Örnek: "/api/v1/products/999"
     */
    private String path;

    /**
     * Validation Hataları (Opsiyonel)
     * @Valid annotation ile gelen hatalar için
     *
     * ÖRNEK:
     * {
     *   "name": "must not be blank",
     *   "price": "must be greater than 0"
     * }
     */
    private Map<String, String> validationErrors;

    /**
     * Detaylı Hata Listesi (Opsiyonel)
     * Birden fazla hata varsa
     */
    private List<String> details;
}