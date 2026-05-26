package com.dresstyle.orderservice.service;

import com.dresstyle.orderservice.dto.OrderRequest;
import com.dresstyle.dto.OrderPlacedEvent;
import com.dresstyle.dto.OrderItemSummary;
import com.dresstyle.orderservice.model.Order;
import com.dresstyle.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.AmqpException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final RabbitTemplate rabbitTemplate;

    public Order createOrder(String email, OrderRequest request) {
        Order order = Order.builder()
                .userId(email)
                .items(request.getItems())
                .shippingAddress(request.getShippingAddress())
                .subtotal(request.getSubtotal())
                .shippingCost(request.getShippingCost())
                .discount(request.getDiscount())
                .total(request.getTotal())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(email);

        publishOrderPlacedEvent(savedOrder, email);

        return savedOrder;
    }

    private void publishOrderPlacedEvent(Order order, String email) {
        try {
            List<OrderItemSummary> items = order.getItems().stream()
                    .map(item -> OrderItemSummary.builder()
                            .id(item.getId())
                            .name(item.getName())
                            .size(item.getSize())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .build())
                    .collect(Collectors.toList());

            OrderPlacedEvent event = OrderPlacedEvent.builder()
                    .email(email)
                    .orderId(order.getId())
                    .total(order.getTotal())
                    .status(order.getStatus())
                    .items(items)
                    .build();

            rabbitTemplate.convertAndSend("notificationExchange", "orderRoutingKey", event);
            log.info("[OrderService] Evento de pedido publicado para {}", order.getId());
        } catch (AmqpException ex) {
            log.warn("[OrderService] Pedido creado pero no se pudo publicar evento para {}", order.getId(), ex);
        }
    }

    public List<Order> getUserOrders(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
