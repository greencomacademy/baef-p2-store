package com.deliveryinsider.store.domain.menu.response;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record MenuResponse(
        Long id,
        String menuName,
        Integer menuPrice,
        Integer menuCost,
        Integer packagingFee,
        Integer expectedCookingTime,
        Integer batchCapacity,
        String menuStatus,
        Integer expectedMargin,
        BigDecimal expectedMarginRate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
