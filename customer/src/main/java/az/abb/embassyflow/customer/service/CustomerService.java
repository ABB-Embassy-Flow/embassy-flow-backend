package az.abb.embassyflow.customer.service;

import az.abb.embassyflow.common.exception.BusinessException;
import az.abb.embassyflow.common.exception.ErrorCodes;
import az.abb.embassyflow.customer.dao.entity.Account;
import az.abb.embassyflow.customer.dao.entity.Card;
import az.abb.embassyflow.customer.dao.entity.Customer;
import az.abb.embassyflow.customer.dao.repository.AccountRepository;
import az.abb.embassyflow.customer.dao.repository.CustomerRepository;
import az.abb.embassyflow.customer.dto.response.AccountResponse;
import az.abb.embassyflow.customer.dto.response.CardResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;

    public CustomerService(CustomerRepository customerRepository, AccountRepository accountRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
    }

    public record CustomerInfo(Long id, String fullName, String phone) {
    }

    @Transactional(readOnly = true)
    public Optional<CustomerInfo> findByFin(String fin) {
        return customerRepository.findByFin(fin)
                .map(this::toInfo);
    }

    @Transactional(readOnly = true)
    public Optional<CustomerInfo> findById(Long id) {
        return customerRepository.findById(id)
                .map(this::toInfo);
    }

    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        return customerRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> accountsFor(Long customerId, Long authenticatedCustomerId) {
        if (authenticatedCustomerId == null || !authenticatedCustomerId.equals(customerId)) {
            throw new BusinessException(ErrorCodes.UNAUTHORIZED, "error.unauthorized", HttpStatus.UNAUTHORIZED);
        }

        if (!customerRepository.existsById(customerId)) {
            throw new BusinessException(
                    ErrorCodes.CUSTOMER_NOT_FOUND, "error.customer_not_found", HttpStatus.NOT_FOUND);
        }

        return accountRepository.findByCustomerIdAndActiveTrueOrderById(customerId).stream()
                .map(CustomerService::toAccountResponse)
                .toList();
    }

    private CustomerInfo toInfo(Customer customer) {
        return new CustomerInfo(customer.getId(), customer.getFullName(), customer.getPhone());
    }

    private static AccountResponse toAccountResponse(Account account) {
        List<CardResponse> cards = account.getCards().stream()
                .filter(Card::isActive)
                .map(card -> new CardResponse(card.getId(), card.getMaskedNumber(),
                        card.getCardBrand(), card.getExpiry()))
                .toList();
        return new AccountResponse(account.getId(), account.getAccountNumber(), account.getCurrency(),
                account.getBalance(), account.getAccountType(), cards);
    }
}