package com.swietek.ecommerce.email;

import lombok.Getter;

@Getter
public enum EmailTemplates {

    PAYMENT_CONFIRMATION("payment-confirmation.html", "E-Commerce :: Payment successfully processed"),
    ORDER_CONFIRMATION("order-confirmation.html", "E-Commerce :: Order confirmation")
    ;

    @Getter
    private final String template;


    @Getter
    private final String subject;


    EmailTemplates(String template, String subject) {
        this.template = template;
        this.subject = subject;
    }
}