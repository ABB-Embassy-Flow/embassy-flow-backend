package az.abb.embassyflow.order.dao.repository;

import az.abb.embassyflow.order.dao.entity.DocumentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentOrderRepository extends JpaRepository<DocumentOrder, Long> {

    boolean existsByVerificationCode(String verificationCode);
}