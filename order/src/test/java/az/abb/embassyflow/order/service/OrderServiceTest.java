package az.abb.embassyflow.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import az.abb.embassyflow.order.dao.entity.DocumentOrder;
import az.abb.embassyflow.order.dao.repository.DocumentOrderRepository;
import az.abb.embassyflow.order.dto.request.CreateOrderRequest;
import az.abb.embassyflow.order.dto.response.OrderCreatedResponse;
import az.abb.embassyflow.order.enums.DocumentType;
import az.abb.embassyflow.order.enums.Language;
import az.abb.embassyflow.order.enums.OrderStatus;
import az.abb.embassyflow.order.enums.TimelineStep;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private DocumentOrderRepository orderRepository;

    @Mock
    private OrderNumberGenerator orderNumberGenerator;

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
}