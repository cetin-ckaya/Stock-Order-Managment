package com.hibernate.stockordermanagment.exception;

import com.hibernate.stockordermanagment.exception.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Yetersiz Stok Exception
 *
 * TASK GEREKSİNİMİ: "InsufficientStockException"
 * TASK KURALLAR:
 * - "Yeterli stok olup olmadığı kontrol edilmelidir"
 * - "Yetersiz stok varsa sipariş oluşturulmamalıdır"
 *
 * NE ZAMAN FIRLATILIR?
 * - Sipariş miktarı > mevcut stok
 * - Örnek: Stokta 5 ürün var, 10 ürün sipariş geldi
 *
 * TASK EXAMPLE:
 * public InsufficientStockException(Long productId, int requested, int available) {
 *     super("Insufficient stock for product: " + productId
 *         + ", requested: " + requested
 *         + ", available: " + available);
 * }
 *
 * HTTP STATUS: 409 CONFLICT
 * - 400 (BAD_REQUEST) de olabilirdi ama CONFLICT daha anlamlı
 * - "İstek doğru ama mevcut durumla çelişiyor" anlamında
 */
public class InsufficientStockException extends BaseException {

    /**
     * TASK formatında constructor
     *
     * @param productId Ürün ID
     * @param requested Talep edilen miktar
     * @param available Mevcut stok miktarı
     *
     * ÖRNEK MESAJ:
     * "Insufficient stock for product: 1, requested: 10, available: 5"
     */
    public InsufficientStockException(Long productId, int requested, int available) {
        super(
                String.format(
                        "Insufficient stock for product: %d, requested: %d, available: %d",
                        productId, requested, available
                ),
                "INSUFFICIENT_STOCK",
                HttpStatus.CONFLICT
        );
    }

    /**
     * Basit versiyon (sadece productId ile)
     *
     * @param productId Ürün ID
     */
    public InsufficientStockException(Long productId) {
        super(
                String.format("Insufficient stock for product: %d", productId),
                "INSUFFICIENT_STOCK",
                HttpStatus.CONFLICT
        );
    }
}