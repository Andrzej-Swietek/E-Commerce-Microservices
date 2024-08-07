package com.swietek.ecommerce.kafka;

import com.swietek.ecommerce.customer.CustomerResponse;
import com.swietek.ecommerce.order.PaymentMethod;
import com.swietek.ecommerce.product.PurchaseResponse;

import java.math.BigDecimal;
import java.util.List;

public record OrderConfirmation (
        String orderReference,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        CustomerResponse customer,
        List<PurchaseResponse> products
) {
}