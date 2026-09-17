package az.abb.embassyflow.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import az.abb.embassyflow.common.exception.BusinessException;
import az.abb.embassyflow.common.exception.ErrorCodes;
import az.abb.embassyflow.customer.service.CustomerService;
import az.abb.embassyflow.order.dao.entity.DocumentOrder;
import az.abb.embassyflow.order.dao.entity.OrderItem;
import az.abb.embassyflow.order.dao.repository.PaymentRepository;
import az.abb.embassyflow.order.dto.request.PayRequest;
import az.abb.embassyflow.order.dto.response.PaymentResponse;
import az.abb.embassyflow.order.enums.DocumentType;
import az.abb.embassyflow.order.enums.Language;
import az.abb.embassyflow.order.enums.OrderStatus;
import az.abb.embassyflow.order.enums.PaymentStatus;
import az.abb.embassyflow.order.enums.Period;
import az.abb.embassyflow.order.enums.StatementType;
import az.abb.embassyflow.order.enums.TimelineStep;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private TransactionNumberGenerator transactionNumberGenerator;

    @Mock
    private CustomerService customerService;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymentService paymentService;

    private DocumentOrder order(OrderStatus status) {
        DocumentOrder order = new DocumentOrder();
        order.setDocumentType(DocumentType.EMBASSY_CERTIFICATE);
        order.setLanguage(Language.AZ);
        order.setStatus(status);
        order.setOrderNumber("AR-2026-000001");
        order.setCustomerId(42L);
        return order;
    }

    private void addItem(DocumentOrder order) {
        OrderItem item = new OrderItem();
        item.setAccountId(11L);
        item.setLanguage(Language.AZ);
        item.setPeriod(Period.ONE_MONTH);
        item.setStatementType(StatementType.ALL);
        order.addItem(item);
    }

    @Test
    void pay_successCreatesPaymentAndAdvancesOrder() {
        DocumentOrder order = order(OrderStatus.OTP_VERIFIED);
        addItem(order);
        when(orderService.requireOwnedOrder(501L, 42L)).thenReturn(order);
        when(transactionNumberGenerator.next()).thenReturn("TXN-2026-00001");
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.pay(501L, new PayRequest(21L, "123"), 42L);

        assertEquals("TXN-2026-00001", response.transactionNo());
        assertEquals(new BigDecimal("10.00"), response.amount());
        assertEquals("AZN", response.currency());
        assertEquals(PaymentStatus.SUCCESS, response.status());
        assertEquals(OrderStatus.PAYMENT_RECEIVED, order.getStatus());
        assertEquals(1, order.getTimeline().size());
        assertEquals(TimelineStep.PAYMENT_RECEIVED, order.getTimeline().get(0).getStep());
        verify(customerService).validateCardForCustomer(21L, 42L);
    }

    @Test
    void pay_wrongStatus_throwsPaymentFailed() {
        DocumentOrder order = order(OrderStatus.CREATED);
        addItem(order);
        when(orderService.requireOwnedOrder(501L, 42L)).thenReturn(order);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> paymentService.pay(501L, new PayRequest(21L, "123"), 42L));

        assertEquals(ErrorCodes.PAYMENT_FAILED, ex.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }

    @Test
    void pay_withoutItems_throwsPaymentFailed() {
        DocumentOrder order = order(OrderStatus.OTP_VERIFIED);
        when(orderService.requireOwnedOrder(501L, 42L)).thenReturn(order);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> paymentService.pay(501L, new PayRequest(21L, "123"), 42L));

        assertEquals(ErrorCodes.PAYMENT_FAILED, ex.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }

    @Test
    void pay_unknownCard_throwsCardNotFound() {
        DocumentOrder order = order(OrderStatus.OTP_VERIFIED);
        addItem(order);
        when(orderService.requireOwnedOrder(501L, 42L)).thenReturn(order);
        doThrow(new BusinessException(ErrorCodes.CARD_NOT_FOUND, "error.card_not_found",
                HttpStatus.NOT_FOUND)).when(customerService).validateCardForCustomer(99L, 42L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> paymentService.pay(501L, new PayRequest(99L, "123"), 42L));

        assertEquals(ErrorCodes.CARD_NOT_FOUND, ex.getCode());
        assertEquals(HttpStatus.NOT_FOUND, ex.getHttpStatus());
    }
}
