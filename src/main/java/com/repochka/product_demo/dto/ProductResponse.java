package com.repochka.product_demo.dto;

import com.repochka.product_demo.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Product response with discount pricing")
public class ProductResponse {

    @Schema(description = "Unique product identifier", example = "1")
    private Long id;

    @Schema(description = "Product name", example = "Clean Code")
    private String name;

    @Schema(description = "Product description", nullable = true, example = "A handbook of agile software craftsmanship")
    private String description;

    @Schema(description = "Original price", example = "39.99")
    private BigDecimal price;

    @Schema(description = "Price after category-based discount", example = "35.99")
    private BigDecimal discountedPrice;

    @Schema(description = "Product category", example = "BOOKS")
    private Category category;

    @Schema(description = "Creation timestamp (UTC, ISO 8601)", example = "2026-02-24T10:30:00Z")
    private OffsetDateTime createdAt;

    @Schema(description = "Last update timestamp (UTC, ISO 8601)", example = "2026-02-24T10:30:00Z")
    private OffsetDateTime updatedAt;
}
