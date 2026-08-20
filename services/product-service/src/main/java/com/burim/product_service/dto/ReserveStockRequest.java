package com.burim.product_service.dto;

import java.util.List;
import java.util.UUID;

public record ReserveStockRequest(
        UUID operationId,
        List<DeductStockRequest> items
) {}