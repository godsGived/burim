package com.burim.order_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOrderRequest(
        @NotNull String userId,
        @NotEmpty @Valid List<OrderItemRequest> items
) {}
