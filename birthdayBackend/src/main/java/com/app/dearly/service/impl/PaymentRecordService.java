package com.app.dearly.service.impl;

import com.app.dearly.dao.PaymentDao;
import com.app.dearly.entity.Payment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Payment rows are written on the request thread and committed before the HTTP
 * response returns. Promo attribution reads this row after Razorpay checkout.
 */
@Service
public class PaymentRecordService {

    private final PaymentDao paymentDao;

    public PaymentRecordService(PaymentDao paymentDao) {
        this.paymentDao = paymentDao;
    }

    @Transactional
    public void recordCreatedOrder(String orderId, double amountInr, String promoCode, String purpose) {
        Payment payment = new Payment();
        payment.setRazorpayOrderId(orderId);
        payment.setAmount(amountInr);
        payment.setCurrency("INR");
        payment.setStatus("CREATED");
        payment.setPromoCode(normalizePromo(promoCode));
        payment.setPurpose(trimPurpose(purpose));
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        paymentDao.save(payment);
    }

    @Transactional
    public void markPaid(String orderId, String paymentId, String signature) {
        Payment payment = paymentDao.findByRazorpayOrderId(orderId).orElse(null);
        if (payment == null) return;
        payment.setRazorpayPaymentId(paymentId);
        payment.setRazorpaySignature(signature);
        payment.setStatus("PAID");
        payment.setUpdatedAt(LocalDateTime.now());
        paymentDao.save(payment);
    }

    @Transactional
    public void markFailed(String orderId) {
        Payment payment = paymentDao.findByRazorpayOrderId(orderId).orElse(null);
        if (payment == null) return;
        payment.setStatus("FAILED");
        payment.setUpdatedAt(LocalDateTime.now());
        paymentDao.save(payment);
    }

    private String normalizePromo(String raw) {
        if (raw == null) return null;
        String code = raw.trim().replaceAll("[^A-Za-z0-9_-]", "").toUpperCase();
        return code.isBlank() ? null : code;
    }

    private String trimPurpose(String raw) {
        if (raw == null) return null;
        String value = raw.trim();
        return value.isEmpty() || "null".equalsIgnoreCase(value) ? null : value;
    }
}
