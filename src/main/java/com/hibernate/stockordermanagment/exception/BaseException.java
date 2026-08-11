package com.hibernate.stockordermanagment.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


/**
 * Tüm Custom Exception'ların Base Sınıfı
 *
 * TASK GEREKSİNİMİ: "Custom exception kullanımı"
 *
 * NEDEN BASE CLASS?
 * - Ortak özellikleri tek yerden yönetmek (errorCode, httpStatus)
 * - Global Exception Handler'da polymorphism kullanabilmek
 * - Kod tekrarını önlemek (DRY: Don't Repeat Yourself)
 *
 * LOMBOK @Getter:
 * - Tüm field'lar için getter metodu oluşturur
 * - Exception'ları handle ederken errorCode ve httpStatus'a erişebiliriz
 */

@Getter
public abstract class BaseException extends RuntimeException{

    private String errorCode;

    private final HttpStatus httpStatus;

    /**
     * Constructor
     *
     * @param message Hata mesajı (Türkçe/İngilizce detaylı açıklama)
     * @param errorCode Hata kodu (sabitse bir enum'dan alınabilir)
     * @param httpStatus HTTP status code
     */

    protected BaseException(String message,String errorCode,HttpStatus httpStatus){
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }


}
