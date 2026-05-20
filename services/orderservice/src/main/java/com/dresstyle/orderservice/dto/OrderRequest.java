package com.dresstyle.orderservice.dto;

import com.dresstyle.orderservice.model.Address;
import com.dresstyle.orderservice.model.OrderItem;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private List<OrderItem> items;
    private Address shippingAddress;
    private double subtotal;
    private double shippingCost;
    private double discount;
    private double total;
}
