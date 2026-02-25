package com.repochka.product_demo.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public enum Category {
    ELECTRONICS(BigDecimal.ZERO),
    BOOKS(new BigDecimal("0.10")),
    FOOD(BigDecimal.ZERO),
    OTHER(BigDecimal.ZERO);

    private final BigDecimal discountRate;
}
