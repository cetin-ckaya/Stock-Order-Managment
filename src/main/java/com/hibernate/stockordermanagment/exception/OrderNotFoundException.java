package com.hibernate.stockordermanagment.exception;

import com.hibernate.stockordermanagment.exception.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Sipariş Bulunamadı Exception
 *
 * TASK GEREKSİNİMİ: "OrderNotFoundException"
 *
 * NE ZAMAN FIRLATILIR?
 * - Olmayan sipariş ID'si ile sorgu yapıldığında
 * - Sipariş iptal edilmeye çalışıldığında ama sipariş yoksa
 *
 * HTTP STATUS: 404 NOT_FOUND
 */
public class OrderNotFoundException extends BaseException {

    public OrderNotFoundException(Long orderId) {
        super(
                String.format("Order not found with id: %d", orderId),
                "ORDER_NOT_FOUND",
                HttpStatus.NOT_FOUND
        );
    }
}