package com.hibernate.stockordermanagment.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler
 *
 * TASK GEREKSİNİMİ: "Exceptionlar global exception handler üzerinden yönetilmelidir"
 *
 * @RestControllerAdvice:
 * - Tüm controller'larda fırlatılan exception'ları yakalar
 * - @ControllerAdvice + @ResponseBody kombinasyonu
 * - JSON response döner
 *
 * @ExceptionHandler:
 * - Hangi exception tiplerini yakalayacağını belirtir
 * - Method parametresi ile exception nesnesine erişiriz
 *
 * @Slf4j (Lombok):
 * - log.error(), log.info() gibi metodlar kullanabiliriz
 * - SLF4J (Simple Logging Facade for Java)
 *
 * ÇALIŞMA PRENSİBİ:
 * 1. Controller'da exception fırlatılır
 * 2. Spring bu class'ı tarar
 * 3. Uygun @ExceptionHandler metodunu bulur
 * 4. ErrorResponse oluşturulur
 * 5. HTTP response olarak dönülür
 */
@Slf4j // Lombok: Logger oluşturur (log.error, log.info kullanabiliriz)
@RestControllerAdvice // Tüm controller'lar için geçerli advice
public class GlobalExceptionHandler {

    /**
     * Base Exception Handler
     * Tüm custom exception'larımız BaseException'dan türediği için
     * hepsini burada yakalayabiliriz
     *
     * TASK: ProductNotFoundException, InsufficientStockException vb. hepsi buraya düşer
     *
     * @param ex BaseException veya alt sınıfları
     * @param request HTTP request bilgisi (path almak için)
     * @return ErrorResponse içeren ResponseEntity
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(
            BaseException ex,
            HttpServletRequest request) {

        // Hatayı logla - Production'da hata takibi için kritik
        log.error("BaseException occurred: {} - {}", ex.getErrorCode(), ex.getMessage());

        // ErrorResponse oluştur
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getHttpStatus().value()) // 404, 400, 409 vb.
                .error(ex.getHttpStatus().getReasonPhrase()) // "Not Found", "Bad Request" vb.
                .message(ex.getMessage()) // "Product not found with id: 999"
                .errorCode(ex.getErrorCode()) // "PRODUCT_NOT_FOUND"
                .path(request.getRequestURI()) // "/api/v1/products/999"
                .build();

        // HTTP Status ile birlikte dön
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(errorResponse);
    }

    /**
     * Validation Exception Handler
     *
     * TASK GEREKSİNİMİ: "Ürün adı boş olamaz", "Fiyat sıfırdan büyük olmalıdır" gibi validasyonlar
     *
     * @Valid annotation ile gelen DTO'larda validation hatası olduğunda fırlatılır
     *
     * ÖRNEK:
     * @NotBlank(message = "Ürün adı boş olamaz")
     * private String name;
     *
     * @DecimalMin(value = "0.01", message = "Fiyat sıfırdan büyük olmalıdır")
     * private BigDecimal price;
     *
     * @param ex MethodArgumentNotValidException
     * @param request HTTP request
     * @return ValidationErrors içeren ErrorResponse
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        log.error("Validation failed: {}", ex.getMessage());

        // Validation hatalarını Map'e çevir
        // Field adı -> Hata mesajı
        Map<String, String> validationErrors = new HashMap<>();

        // STREAM API KULLANIMI - TASK GEREKSİNİMİ
        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    validationErrors.put(fieldName, errorMessage);
                });

        // ErrorResponse oluştur
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation failed") // Genel mesaj
                .errorCode("VALIDATION_ERROR")
                .path(request.getRequestURI())
                .validationErrors(validationErrors) // Detaylı hatalar
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * IllegalArgumentException Handler
     *
     * Product entity içinde decreaseStock metodunda fırlatıyoruz
     * Business logic validation'ları için kullanılır
     *
     * @param ex IllegalArgumentException
     * @param request HTTP request
     * @return ErrorResponse
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.error("IllegalArgumentException: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .errorCode("INVALID_ARGUMENT")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Generic Exception Handler
     * Yakalanmayan tüm exception'lar buraya düşer
     *
     * PRODUCTION'DA ÇOK ÖNEMLİ:
     * - Beklenmeyen hatalar olabilir
     * - Stack trace client'a gitmemeli (güvenlik riski)
     * - Hata detayları loglara yazılmalı
     *
     * @param ex Exception
     * @param request HTTP request
     * @return Generic ErrorResponse
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        // Stack trace'i logla (production'da kritik)
        log.error("Unexpected error occurred", ex);

        ErrorResponse errorResponse = com.hibernate.stockordermanagment.exception.ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error occurred") // Generic mesaj
                .errorCode("INTERNAL_SERVER_ERROR")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}