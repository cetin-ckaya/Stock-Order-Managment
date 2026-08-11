package com.hibernate.stockordermanagment.exception;

import com.hibernate.stockordermanagment.exception.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Ürün Aktif Değil Exception
 *
 * TASK GEREKSİNİMİ: "ProductNotActiveException"
 * TASK KURAL: "Sipariş oluşturulurken ürünlerin aktif olup olmadığı kontrol edilmelidir"
 *
 * NE ZAMAN FIRLATILIR?
 * - Status = PASSIVE veya OUT_OF_STOCK olan ürüne sipariş verilmeye çalışıldığında
 *
 * İŞ KURALI:
 * - Sadece ACTIVE ürünler sipariş edilebilir
 * - PASSIVE: Ürün satışa kapalı (sezonluk ürün, geçici olarak satışta değil)
 * - OUT_OF_STOCK: Stok bitti, sipariş alınamaz
 *
 * HTTP STATUS: 400 BAD_REQUEST
 * - Client hatalı istek gönderdi
 */
public class ProductNotActiveException extends BaseException {

    /**
     * Constructor
     *
     * @param productId Pasif ürünün ID'si
     * @param currentStatus Ürünün mevcut durumu (PASSIVE, OUT_OF_STOCK)
     */
    public ProductNotActiveException(Long productId, String currentStatus) {
        super(
                String.format(
                        "Product with id: %d is not active. Current status: %s",
                        productId, currentStatus
                ),
                "PRODUCT_NOT_ACTIVE",
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Basit versiyon
     */
    public ProductNotActiveException(Long productId) {
        super(
                String.format("Product with id: %d is not active", productId),
                "PRODUCT_NOT_ACTIVE",
                HttpStatus.BAD_REQUEST
        );
    }
}