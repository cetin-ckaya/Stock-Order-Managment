package com.hibernate.stockordermanagment.enums;
/**
 * Sipariş Durumu Enum
 *
 * TASK GEREKSİNİMİ: "public enum OrderStatus { CREATED, APPROVED, CANCELLED, COMPLETED }"
 *
 * Sipariş yaşam döngüsünü yönetir:
 * CREATED → APPROVED → COMPLETED (Normal akış)
 * CREATED → CANCELLED (İptal durumu)
 *
 * STATE MACHINE mantığı:
 * - CREATED: Sipariş oluşturuldu, henüz onaylanmadı
 * - APPROVED: Sipariş onaylandı, hazırlanıyor
 * - COMPLETED: Sipariş tamamlandı, müşteriye ulaştı
 * - CANCELLED: Sipariş iptal edildi, stoklar geri eklendi
 *
 * TASK KURALI: "Tamamlanmış sipariş iptal edilemez" - Bu enum sayesinde kontrol edeceğiz
 */

public enum OrderStatus {
    CREATED,
    APPROVED,
    CANCELLED,
    COMPLETED
}
