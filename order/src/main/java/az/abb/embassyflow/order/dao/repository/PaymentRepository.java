package az.abb.embassyflow.order.dao.repository;

import az.abb.embassyflow.order.dao.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
