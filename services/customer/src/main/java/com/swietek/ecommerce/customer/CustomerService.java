package com.swietek.ecommerce.customer;

import com.swietek.ecommerce.exception.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    public List<CustomerResponse> findAllCustomers() {
        return  this.repository.findAll().stream()
                .map(this.mapper::fromCustomer)
                .collect(Collectors.toList());
    }

    public CustomerResponse findById(String customerId) {
        return repository.findById(customerId)
                .map(mapper::fromCustomer)
                .orElseThrow(()-> new CustomerNotFoundException("Customer not found with id " + customerId));
    }

    public String createCustomer(CustomerRequest request) {
        var customer = repository.save(mapper.toCustomer(request));
        return customer.getId();
    }

    public void updateCustomer(CustomerRequest request) {
        var customer = repository.findById(request.id())
                .orElseThrow(()-> new CustomerNotFoundException(
                        format("Cannot Update customer :: No customer Found with id %s", request.id())
                ));

        mergeCustomer(customer, request);
        repository.save(customer);
    }

    public Boolean existsById(String customerId) {
        return repository.findById(customerId).isPresent();
    }


    private void mergeCustomer(Customer customer, CustomerRequest request) {
        if (StringUtils.isNotBlank(request.firstname())) {
            customer.setFirstname(request.firstname());
        }
        if (StringUtils.isNotBlank(request.email())) {
            customer.setEmail(request.email());
        }
        if (request.address() != null) {
            customer.setAddress(request.address());
        }
    }

    public void deleteCustomer(String customerId) {
        repository.deleteById(customerId);
    }
}
