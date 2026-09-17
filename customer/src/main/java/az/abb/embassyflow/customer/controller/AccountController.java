package az.abb.embassyflow.customer.controller;

import az.abb.embassyflow.common.web.AuthAttributes;
import az.abb.embassyflow.customer.dto.response.CustomerAccountsResponse;
import az.abb.embassyflow.customer.service.CustomerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
public class AccountController {

    private final CustomerService customerService;

    public AccountController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/{customerId}/accounts")
    public CustomerAccountsResponse accounts(@PathVariable Long customerId,
                                             @RequestAttribute(name = AuthAttributes.CUSTOMER_ID,
                                                     required = false) Long authenticatedCustomerId) {
        return new CustomerAccountsResponse(customerService.accountsFor(customerId, authenticatedCustomerId));
    }
}