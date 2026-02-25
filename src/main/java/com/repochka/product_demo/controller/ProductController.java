package com.repochka.product_demo.controller;

import com.repochka.product_demo.dto.ProductCreateRequest;
import com.repochka.product_demo.dto.ProductResponse;
import com.repochka.product_demo.dto.ProductUpdateRequest;
import com.repochka.product_demo.entity.Category;
import com.repochka.product_demo.exception.ErrorResponse;
import com.repochka.product_demo.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product CRUD operations with category-based discount pricing")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Create a product", description = "Creates a new product with validation and returns it with a calculated discounted price")
    @ApiResponse(responseCode = "201", description = "Product created")
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        log.info("Creating product with name={}", request.getName());
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "List products", description = "Returns a paginated list of active products, optionally filtered by category")
    @ApiResponse(responseCode = "200", description = "Paginated product list")
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> listProducts(
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable,
            @Parameter(description = "Filter by product category") @RequestParam(required = false) Category category) {
        log.info("Listing products page={} size={} category={}", pageable.getPageNumber(), pageable.getPageSize(), category);
        Page<ProductResponse> products = productService.listProducts(pageable, category);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Get a product by ID", description = "Returns a single active product by its unique identifier")
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@Parameter(description = "Product ID") @PathVariable Long id) {
        log.info("Fetching product id={}", id);
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update a product", description = "Full replacement of a product's mutable fields. Omitted optional fields are set to null.")
    @ApiResponse(responseCode = "200", description = "Product updated")
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        log.info("Updating product id={}", id);
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a product", description = "Soft-deletes a product. The product is retained in storage but excluded from all retrieval operations.")
    @ApiResponse(responseCode = "200", description = "Product deleted")
    @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@Parameter(description = "Product ID") @PathVariable Long id) {
        log.info("Deleting product id={}", id);
        productService.softDeleteProduct(id);
        return ResponseEntity.ok().build();
    }
}
