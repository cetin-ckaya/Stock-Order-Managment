package com.hibernate.stockordermanagment.enums;
/**
 * Ürün Durumu Enum
 *
 * TASK GEREKSİNİMİ: "Enum kullanımı" - Ürün durumlarını enum ile yönetiyoruz
 *
 * NEDEN ENUM?
 * 1. Type-safety: Sadece belirli değerler atanabilir (ACTIVE, PASSIVE, OUT_OF_STOCK)
 * 2. Kod okunabilirliği: "1" yerine "ACTIVE" yazmak daha anlaşılır
 * 3. Refactoring kolaylığı: Tüm kullanımları IDE üzerinden bulabilirsiniz
 * 4. Compile-time check: Yanlış değer yazarsanız derleme hatası alırsınız
 *
 * String yerine Enum kullanmanın avantajları:
 * - Database'de tutarlılık: Yanlış değer girmek imkansız
 * - Magic string'lerden kurtulma: "active", "ACTIVE", "Active" gibi farklı yazımlar olmaz
 */

public enum ProductStatus {
    ACTIVE,
    PASSIVE,
    OUT_OF_STOCK
}
