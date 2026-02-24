package com.repochka.product_demo.dto;

import com.repochka.product_demo.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private Category category;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
