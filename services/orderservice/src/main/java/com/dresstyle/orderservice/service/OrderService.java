package com.dresstyle.orderservice.service;

import com.dresstyle.orderservice.dto.OrderRequest;
import com.dresstyle.orderservice.model.Order;
import com.dresstyle.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;

    public Order createOrder(String userId, OrderRequest request) {
        Order order = Order.builder()
                .userId(userId)
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
        
        cartService.clearCart(userId);
        
        return savedOrder;
    }

    public List<Order> getUserOrders(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
