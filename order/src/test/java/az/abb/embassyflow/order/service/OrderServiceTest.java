package az.abb.embassyflow.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import az.abb.embassyflow.common.exception.BusinessException;
import az.abb.embassyflow.common.exception.ErrorCodes;
import az.abb.embassyflow.customer.service.CustomerService;
import az.abb.embassyflow.embassy.service.EmbassyService;
import az.abb.embassyflow.order.dao.entity.DocumentOrder;
import az.abb.embassyflow.order.dao.repository.DocumentOrderRepository;
import az.abb.embassyflow.order.dto.request.CreateOrderRequest;
import az.abb.embassyflow.order.dto.request.UpdateOrderRequest;
import az.abb.embassyflow.order.dto.response.OrderCreatedResponse;
import az.abb.embassyflow.order.dto.response.OrderUpdatedResponse;
import az.abb.embassyflow.order.enums.DocumentType;
import az.abb.embassyflow.order.enums.Language;
import az.abb.embassyflow.order.enums.OrderStatus;
import az.abb.embassyflow.order.enums.TimelineStep;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private DocumentOrderRepository orderRepository;

    @Mock
    private OrderNumberGenerator orderNumberGenerator;

    @Mock
    private EmbassyService embassyService;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createDraft_persistsOrderWithNumberAndTimeline() {
        when(orderNumberGenerator.next()).thenReturn("AR-2026-000001");
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderRequest request = new CreateOrderRequest(DocumentType.EMBASSY_CERTIFICATE, Language.AZ);
        OrderCreatedResponse response = orderService.createDraft(request);

        ArgumentCaptor<DocumentOrder> captor = ArgumentCaptor.forClass(DocumentOrder.class);
        verify(orderRepository).save(captor.capture());

        DocumentOrder saved = captor.getValue();
        assertEquals("AR-2026-000001", response.orderNumber());
        assertEquals("CREATED", response.status());
        assertEquals(DocumentType.EMBASSY_CERTIFICATE, saved.getDocumentType());
        assertEquals(Language.AZ, saved.getLanguage());
        assertEquals(OrderStatus.CREATED, saved.getStatus());
        assertEquals("AR-2026-000001", saved.getOrderNumber());
        assertEquals(1, saved.getTimeline().size());
        assertEquals(TimelineStep.ORDER_RECEIVED, saved.getTimeline().get(0).getStep());
        assertNotNull(saved.getTimeline().get(0).getOrder());
    }

    private DocumentOrder order(DocumentType type, OrderStatus status) {
        DocumentOrder order = new DocumentOrder();
        order.setDocumentType(type);
        order.setLanguage(Language.AZ);
        order.setStatus(status);
        order.setOrderNumber("AR-2026-000001");
        return order;
    }

    @Test
    void updateOrder_updatesEmbassyAndLanguage() {
        DocumentOrder order = order(DocumentType.EMBASSY_CERTIFICATE, OrderStatus.CREATED);
        when(orderRepository.findById(501L)).thenReturn(Optional.of(order));
        when(embassyService.exists(1L)).thenReturn(true);

        OrderUpdatedResponse response = orderService.updateOrder(501L, new UpdateOrderRequest(1L, Language.EN));

        assertEquals(1L, response.embassyId());
        assertEquals(Language.EN, response.language());
        assertEquals("CREATED", response.status());
        assertEquals(1L, order.getEmbassyId());
        assertEquals(Language.EN, order.getLanguage());
    }

    @Test
    void updateOrder_orderNotFound_throwsOrderNotFound() {
        when(orderRepository.findById(501L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.updateOrder(501L, new UpdateOrderRequest(1L, Language.EN)));

        assertEquals(ErrorCodes.ORDER_NOT_FOUND, ex.getCode());
        assertEquals(HttpStatus.NOT_FOUND, ex.getHttpStatus());
    }

    @Test
    void updateOrder_certificateWithoutEmbassy_throwsValidationError() {
        DocumentOrder order = order(DocumentType.EMBASSY_CERTIFICATE, OrderStatus.CREATED);
        when(orderRepository.findById(501L)).thenReturn(Optional.of(order));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.updateOrder(501L, new UpdateOrderRequest(null, Language.AZ)));

        assertEquals(ErrorCodes.VALIDATION_ERROR, ex.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }

    @Test
    void updateOrder_unknownEmbassy_throwsEmbassyNotFound() {
        DocumentOrder order = order(DocumentType.EMBASSY_CERTIFICATE, OrderStatus.CREATED);
        when(orderRepository.findById(501L)).thenReturn(Optional.of(order));
        when(embassyService.exists(999L)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.updateOrder(501L, new UpdateOrderRequest(999L, Language.AZ)));

        assertEquals(ErrorCodes.EMBASSY_NOT_FOUND, ex.getCode());
        assertEquals(HttpStatus.NOT_FOUND, ex.getHttpStatus());
    }

    @Test
    void linkIdentity_linksCustomerAndAdvancesStatus() {
        DocumentOrder order = order(DocumentType.EMBASSY_CERTIFICATE, OrderStatus.CREATED);
        when(customerService.exists(42L)).thenReturn(true);
        when(orderRepository.findById(501L)).thenReturn(Optional.of(order));

        OrderUpdatedResponse response = orderService.linkIdentity(501L, 42L, 42L);

        assertEquals("OTP_VERIFIED", response.status());
        assertEquals(42L, order.getCustomerId());
        assertEquals(OrderStatus.OTP_VERIFIED, order.getStatus());
        assertEquals(1, order.getTimeline().size());
        assertEquals(TimelineStep.OTP_VERIFIED, order.getTimeline().get(0).getStep());
    }

    @Test
    void linkIdentity_missingToken_throwsUnauthorized() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.linkIdentity(501L, 42L, null));

        assertEquals(ErrorCodes.UNAUTHORIZED, ex.getCode());
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getHttpStatus());
    }

    @Test
    void linkIdentity_tokenMismatch_throwsUnauthorized() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.linkIdentity(501L, 42L, 43L));

        assertEquals(ErrorCodes.UNAUTHORIZED, ex.getCode());
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getHttpStatus());
    }

    @Test
    void linkIdentity_unknownCustomer_throwsCustomerNotFound() {
        when(customerService.exists(42L)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.linkIdentity(501L, 42L, 42L));

        assertEquals(ErrorCodes.CUSTOMER_NOT_FOUND, ex.getCode());
        assertEquals(HttpStatus.NOT_FOUND, ex.getHttpStatus());
    }

    @Test
    void linkIdentity_orderNotFound_throwsOrderNotFound() {
        when(customerService.exists(42L)).thenReturn(true);
        when(orderRepository.findById(501L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.linkIdentity(501L, 42L, 42L));

        assertEquals(ErrorCodes.ORDER_NOT_FOUND, ex.getCode());
        assertEquals(HttpStatus.NOT_FOUND, ex.getHttpStatus());
    }
}