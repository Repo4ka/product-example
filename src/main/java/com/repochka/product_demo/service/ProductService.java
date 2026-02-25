package com.repochka.product_demo.service;

import com.repochka.product_demo.dto.ProductCreateRequest;
import com.repochka.product_demo.dto.ProductResponse;
import com.repochka.product_demo.dto.ProductUpdateRequest;
import com.repochka.product_demo.entity.Category;
import com.repochka.product_demo.entity.Product;
import com.repochka.product_demo.exception.ProductNotFoundException;
import com.repochka.product_demo.mapper.ProductMapper;
import com.repochka.product_demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductResponse createProduct(ProductCreateRequest request) {
        Product product = productMapper.toEntity(request);
        Product saved = productRepository.save(product);
        log.info("Created product id={}", saved.getId());
        return toResponseWithDiscount(saved);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        log.debug("Retrieving product id={}", id);
        Product product = findActiveProductOrThrow(id);
        return toResponseWithDiscount(product);
    }

    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = findActiveProductOrThrow(id);
        productMapper.updateEntityFromRequest(request, product);
        Product saved = productRepository.save(product);
        log.info("Updated product id={}", saved.getId());
        return toResponseWithDiscount(saved);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listProducts(Pageable pageable, Category category) {
        log.info("Listing products page={} size={} category={}", pageable.getPageNumber(), pageable.getPageSize(), category);
        Page<Product> products = (category != null)
                ? productRepository.findAllByCategoryAndDeletedFalse(category, pageable)
                : productRepository.findAllByDeletedFalse(pageable);
        return products.map(this::toResponseWithDiscount);
    }

    public void softDeleteProduct(Long id) {
        Product product = findActiveProductOrThrow(id);
        product.setDeleted(true);
        product.setDeletedAt(OffsetDateTime.now());
        productRepository.save(product);
        log.info("Soft-deleted product id={}", id);
    }

    private Product findActiveProductOrThrow(Long id) {
        return productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private ProductResponse toResponseWithDiscount(Product product) {
        ProductResponse response = productMapper.toResponse(product);
        response.setDiscountedPrice(product.getPrice());
        return response;
    }
}
