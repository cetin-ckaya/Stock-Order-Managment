package com.hibernate.stockordermanagment.exception;

import com.hibernate.stockordermanagment.exception.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Geçersiz Sipariş Exception
 *
 * TASK GEREKSİNİMİ: "InvalidOrderException"
 *
 * NE ZAMAN FIRLATILIR?
 * - Sipariş ürün listesi boş
 * - Müşteri adı boş
 * - Miktar <= 0
 * - Tamamlanmış sipariş iptal edilmeye çalışıldığında
 * - Zaten iptal edilmiş sipariş tekrar iptal edilmeye çalışıldığında
 *
 * TASK KURALLAR:
 * - "Zaten iptal edilmiş sipariş tekrar iptal edilememelidir"
 * - "Tamamlanmış sipariş iptal edilememelidir"
 *
 * HTTP STATUS: 400 BAD_REQUEST
 * - Client hatalı istek gönderdi
 */
public class InvalidOrderException extends BaseException {

    /**
     * Genel geçersiz sipariş hatası
     */
    public InvalidOrderException(String message) {
        super(message, "INVALID_ORDER", HttpStatus.BAD_REQUEST);
    }

    /**
     * Sipariş durumu nedeniyle geçersiz
     * Örnek: COMPLETED siparişi iptal etmeye çalışma
     */
    public InvalidOrderException(Long orderId, String reason) {
        super(
                String.format("Invalid operation on order %d: %s", orderId, reason),
                "INVALID_ORDER",
                HttpStatus.BAD_REQUEST
        );
    }
}