package az.abb.embassyflow.order.service;

import az.abb.embassyflow.common.exception.BusinessException;
import az.abb.embassyflow.common.exception.ErrorCodes;
import az.abb.embassyflow.embassy.service.EmbassyService;
import az.abb.embassyflow.order.dao.entity.DocumentOrder;
import az.abb.embassyflow.order.dao.repository.DocumentOrderRepository;
import az.abb.embassyflow.order.dto.request.CreateOrderRequest;
import az.abb.embassyflow.order.dto.request.UpdateOrderRequest;
import az.abb.embassyflow.order.dto.response.OrderCreatedResponse;
import az.abb.embassyflow.order.dto.response.OrderUpdatedResponse;
import az.abb.embassyflow.order.enums.DocumentType;
import az.abb.embassyflow.order.enums.OrderStatus;
import az.abb.embassyflow.order.enums.TimelineStep;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final DocumentOrderRepository orderRepository;
    private final OrderNumberGenerator orderNumberGenerator;
    private final EmbassyService embassyService;

    public OrderService(DocumentOrderRepository orderRepository, OrderNumberGenerator orderNumberGenerator,
                        EmbassyService embassyService) {
        this.orderRepository = orderRepository;
        this.orderNumberGenerator = orderNumberGenerator;
        this.embassyService = embassyService;
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

    @Transactional
    public OrderUpdatedResponse updateOrder(Long orderId, UpdateOrderRequest request) {
        DocumentOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCodes.ORDER_NOT_FOUND, "error.order_not_found", HttpStatus.NOT_FOUND));

        if (order.getDocumentType() == DocumentType.EMBASSY_CERTIFICATE && request.embassyId() == null) {
            throw new BusinessException(
                    ErrorCodes.VALIDATION_ERROR, "error.embassy_required", HttpStatus.BAD_REQUEST);
        }

        if (request.embassyId() != null && !embassyService.exists(request.embassyId())) {
            throw new BusinessException(
                    ErrorCodes.EMBASSY_NOT_FOUND, "error.embassy_not_found", HttpStatus.NOT_FOUND);
        }

        order.setEmbassyId(request.embassyId());
        order.setLanguage(request.language());

        return new OrderUpdatedResponse(order.getId(), order.getEmbassyId(), order.getLanguage(),
                order.getStatus().name());
    }
}