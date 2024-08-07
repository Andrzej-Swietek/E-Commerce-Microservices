package com.swietek.ecommerce.payment;

import com.swietek.ecommerce.customer.CustomerResponse;
import com.swietek.ecommerce.order.PaymentMethod;

import java.math.BigDecimal;

public record PaymentRequest(
        BigDecimal amount,
        PaymentMethod paymentMethod,
        Integer orderId,
        String orderReference,
        CustomerResponse customer
) {
}