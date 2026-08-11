package com.hibernate.stockordermanagment.exception;

import com.hibernate.stockordermanagment.exception.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Tekrarlı Ürün Exception
 *
 * TASK GEREKSİNİMİ: "DuplicateProductException"
 * TASK KURAL: "Aynı isimle ikinci bir aktif ürün oluşturulmamalıdır"
 *
 * NE ZAMAN FIRLATILIR?
 * - Veritabanında zaten "Kablosuz Kulaklık" isimli ACTIVE ürün varken
 * - Tekrar "Kablosuz Kulaklık" isimli ürün oluşturulmaya çalışıldığında
 *
 * ÖNEMLİ: PASSIVE durumundaki ürün varsa yeni ürün oluşturulabilir
 * - ACTIVE + "Kulaklık" → Var
 * - PASSIVE + "Kulaklık" → Var
 * - Yeni ürün: "Kulaklık" → İZİN VER (çünkü aktif olan yok)
 *
 * DATABASE CONSTRAINT:
 * - UNIQUE constraint name + status üzerinde olabilir
 * - Ya da service layer'da kontrol edilir
 *
 * HTTP STATUS: 409 CONFLICT
 * - Kaynak zaten mevcut, çelişki var
 */
public class DuplicateProductException extends BaseException {

    /**
     * Constructor
     *
     * @param productName Tekrarlayan ürün adı
     */
    public DuplicateProductException(String productName) {
        super(
                String.format(
                        "An active product with name '%s' already exists",
                        productName
                ),
                "DUPLICATE_PRODUCT",
                HttpStatus.CONFLICT
        );
    }
}