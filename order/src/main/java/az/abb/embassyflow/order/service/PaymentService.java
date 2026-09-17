package az.abb.embassyflow.order.service;

import az.abb.embassyflow.common.exception.BusinessException;
import az.abb.embassyflow.common.exception.ErrorCodes;
import az.abb.embassyflow.customer.service.CustomerService;
import az.abb.embassyflow.order.dao.entity.DocumentOrder;
import az.abb.embassyflow.order.dao.entity.Payment;
import az.abb.embassyflow.order.dao.repository.PaymentRepository;
import az.abb.embassyflow.order.dto.request.PayRequest;
import az.abb.embassyflow.order.dto.response.PaymentResponse;
import az.abb.embassyflow.order.enums.OrderStatus;
import az.abb.embassyflow.order.enums.PaymentStatus;
import az.abb.embassyflow.order.enums.TimelineStep;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TransactionNumberGenerator transactionNumberGenerator;
    private final CustomerService customerService;
    private final OrderService orderService;

    public PaymentService(PaymentRepository paymentRepository,
                          TransactionNumberGenerator transactionNumberGenerator,
                          CustomerService customerService, OrderService orderService) {
        this.paymentRepository = paymentRepository;
        this.transactionNumberGenerator = transactionNumberGenerator;
        this.customerService = customerService;
        this.orderService = orderService;
    }

    @Transactional
    public PaymentResponse pay(Long orderId, PayRequest request, Long authenticatedCustomerId) {
        DocumentOrder order = orderService.requireOwnedOrder(orderId, authenticatedCustomerId);

        if (order.getStatus() != OrderStatus.OTP_VERIFIED || order.getItems().isEmpty()) {
            throw new BusinessException(
                    ErrorCodes.PAYMENT_FAILED, "error.payment_failed", HttpStatus.BAD_REQUEST);
        }

        customerService.validateCardForCustomer(request.cardId(), order.getCustomerId());

        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setCardId(request.cardId());
        payment.setAmount(order.getDocumentType().getPrice());
        payment.setCurrency(order.getDocumentType().getCurrency());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionNo(transactionNumberGenerator.next());

        Payment saved = paymentRepository.save(payment);

        order.setStatus(OrderStatus.PAYMENT_RECEIVED);
        order.addTimeline(TimelineStep.PAYMENT_RECEIVED);

        return new PaymentResponse(saved.getId(), saved.getTransactionNo(), saved.getAmount(),
                saved.getCurrency(), saved.getStatus());
    }
}
