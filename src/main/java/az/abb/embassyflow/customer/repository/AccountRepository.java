package az.abb.embassyflow.customer.repository;

import az.abb.embassyflow.customer.domain.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @EntityGraph(attributePaths = {"cards"})
    List<Account> findByCustomerId(Long customerId);
}
