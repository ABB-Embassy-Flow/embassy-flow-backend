package az.abb.embassyflow.order.service;

import az.abb.embassyflow.order.dao.entity.DocumentOrder;
import az.abb.embassyflow.order.dao.repository.DocumentOrderRepository;
import az.abb.embassyflow.order.dto.request.CreateOrderRequest;
import az.abb.embassyflow.order.dto.response.OrderCreatedResponse;
import az.abb.embassyflow.order.enums.OrderStatus;
import az.abb.embassyflow.order.enums.TimelineStep;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final DocumentOrderRepository orderRepository;
    private final OrderNumberGenerator orderNumberGenerator;

    public OrderService(DocumentOrderRepository orderRepository, OrderNumberGenerator orderNumberGenerator) {
        this.orderRepository = orderRepository;
        this.orderNumberGenerator = orderNumberGenerator;
    }

    @Transactional
    public OrderCreatedResponse createDraft(CreateOrderRequest request) {
        DocumentOrder order = new DocumentOrder();
        order.setDocumentType(request.documentType());
        order.setLanguage(request.language());
        order.setStatus(OrderStatus.CREATED);
        order.setOrderNumber(orderNumberGenerator.next());
        order.addTimeline(TimelineStep.ORDER_RECEIVED);

        DocumentOrder saved = orderRepository.save(order);

        return new OrderCreatedResponse(saved.getId(), saved.getOrderNumber(), saved.getStatus().name());
    }
}