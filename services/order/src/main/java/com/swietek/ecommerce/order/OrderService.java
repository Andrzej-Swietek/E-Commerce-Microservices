package com.swietek.ecommerce.order;

import com.swietek.ecommerce.customer.CustomerClient;
import com.swietek.ecommerce.exception.BusinessException;
import com.swietek.ecommerce.kafka.OrderConfirmation;
import com.swietek.ecommerce.kafka.OrderProducer;
import com.swietek.ecommerce.orderline.OrderLineRequest;
import com.swietek.ecommerce.orderline.OrderLineService;
import com.swietek.ecommerce.payment.PaymentClient;
import com.swietek.ecommerce.payment.PaymentRequest;
import com.swietek.ecommerce.product.ProductClient;
import com.swietek.ecommerce.product.PurchaseRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;

    private final CustomerClient customerClient;
    private final ProductClient productClient;
    private final PaymentClient paymentClient;
    private final OrderProducer orderProducer;

    private final OrderLineService orderLineService;


    public OrderResponse findById(Integer orderId) {
        return this.repository.findById(orderId)
                .map(this.mapper::fromOrder)
                .orElseThrow(() -> new EntityNotFoundException(String.format("No order found with the provided ID: %d", orderId)));
    }

    public List<OrderResponse> findAllOrders() {
        return this.repository.findAll()
                .stream()
                .map(this.mapper::fromOrder)
                .collect(Collectors.toList());
    }

    @Transactional
    public Integer createOrder(OrderRequest request) {

        // 1. Check Customer            --> customers microservice (OpenFeign)
        var customer = this.customerClient.findCustomerById(request.customerId())
                .orElseThrow(() -> new BusinessException("Cannot create order:: No customer exists with the provided ID"));


        // 2. Purchase the Products     --> products microservice (Rest Template)
        var purchasedProducts = productClient.purchaseProducts(request.products());


        // 3. Persist Order data and Order Lines
        var order = this.repository.save(mapper.toOrder(request));
        for (PurchaseRequest purchaseRequest : request.products()) {
            orderLineService.saveOrderLine(
                    new OrderLineRequest(
                            null,
                            order.getId(),
                            purchaseRequest.productId(),
                            purchaseRequest.quantity()
                    )
            );
        }


        // 4. Start Payment process    --> payment microservice
        var paymentRequest = new PaymentRequest(
                request.amount(),
                request.paymentMethod(),
                order.getId(),
                order.getReference(),
                customer
        );
        paymentClient.requestOrderPayment(paymentRequest);


        // 5. Send Order confirmations --> notification microservice (kafka)
        orderProducer.sendOrderConfirmation(
                new OrderConfirmation(
                        request.reference(),
                        request.amount(),
                        request.paymentMethod(),
                        customer,
                        purchasedProducts
                )
        );

        return order.getId();
    }
}
