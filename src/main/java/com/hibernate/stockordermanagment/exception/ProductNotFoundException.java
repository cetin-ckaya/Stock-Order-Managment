package com.hibernate.stockordermanagment.exception;

import com.hibernate.stockordermanagment.exception.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Ürün Bulunamadı Exception
 *
 * TASK GEREKSİNİMİ: "ProductNotFoundException"
 *
 * NE ZAMAN FIRLATILIR?
 * - Olmayan bir ürün ID'si ile sorgu yapıldığında
 * - Sipariş oluşturulurken gönderilen productId veritabanında yoksa
 *
 * ÖRNEK KULLANIM:
 * Product product = productRepository.findById(id)
 *     .orElseThrow(() -> new ProductNotFoundException(id));
 *
 * HTTP STATUS: 404 NOT_FOUND
 * - Client'a "Bu ürün bulunamadı" mesajı gider
 */
public class ProductNotFoundException extends BaseException {

    /**
     * ID ile ürün bulunamadığında
     *
     * @param productId Bulunamayan ürünün ID'si
     */
    public ProductNotFoundException(Long productId) {
        super(
                String.format("Product not found with id: %d", productId),
                "PRODUCT_NOT_FOUND",
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * İsim ile ürün bulunamadığında (optional)
     *
     * @param productName Bulunamayan ürünün adı
     */
    public ProductNotFoundException(String productName) {
        super(
                String.format("Product not found with name: %s", productName),
                "PRODUCT_NOT_FOUND",
                HttpStatus.NOT_FOUND
        );
    }
}