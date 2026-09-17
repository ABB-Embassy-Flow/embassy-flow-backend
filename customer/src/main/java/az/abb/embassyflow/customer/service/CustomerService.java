package az.abb.embassyflow.customer.service;

import az.abb.embassyflow.customer.dao.entity.Customer;
import az.abb.embassyflow.customer.dao.repository.CustomerRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
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

    private CustomerInfo toInfo(Customer customer) {
        return new CustomerInfo(customer.getId(), customer.getFullName(), customer.getPhone());
    }
}