package az.abb.embassyflow.customer.web;

import az.abb.embassyflow.customer.domain.Account;
import az.abb.embassyflow.customer.domain.Card;
import az.abb.embassyflow.customer.service.CustomerService;
import az.abb.embassyflow.customer.web.dto.response.AccountResponse;
import az.abb.embassyflow.customer.web.dto.response.CardResponse;
import az.abb.embassyflow.customer.web.dto.response.CustomerAccountsResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class AccountController {

    private final CustomerService customerService;

    public AccountController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/{customerId}/accounts")
    public CustomerAccountsResponse getAccounts(@PathVariable Long customerId) {
        List<AccountResponse> accounts = customerService.accountsFor(customerId).stream()
                .map(AccountController::toResponse)
                .toList();
        return new CustomerAccountsResponse(accounts);
    }

    private static AccountResponse toResponse(Account account) {
        List<CardResponse> cards = account.getCards().stream()
                .filter(Card::isActive)
                .map(card -> new CardResponse(card.getId(), card.getMaskedNumber(),
                        card.getCardBrand(), card.getExpiry()))
                .toList();
        return new AccountResponse(account.getId(), account.getAccountNumber(),
                account.getCurrency(), account.getBalance(), account.getAccountType(), cards);
    }
}