package az.abb.embassyflow.customer.service;

import az.abb.embassyflow.common.BusinessException;
import az.abb.embassyflow.common.ErrorCodes;
import az.abb.embassyflow.customer.domain.Account;
import az.abb.embassyflow.customer.domain.Customer;
import az.abb.embassyflow.customer.repository.AccountRepository;
import az.abb.embassyflow.customer.repository.CustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;

    public CustomerService(CustomerRepository customerRepository, AccountRepository accountRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public Customer findById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, ErrorCodes.CUSTOMER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Customer findByFin(String fin) {
        return customerRepository.findByFin(fin)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, ErrorCodes.CUSTOMER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<Account> accountsFor(Long customerId) {
        findById(customerId);
        return accountRepository.findByCustomerId(customerId);
    }
}
