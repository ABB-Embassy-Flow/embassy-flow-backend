package az.abb.embassyflow.order.controller;

import az.abb.embassyflow.order.dto.request.CreateOrderRequest;
import az.abb.embassyflow.order.dto.response.OrderCreatedResponse;
import az.abb.embassyflow.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderCreatedResponse createDraft(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createDraft(request);
    }
}