package com.repochka.product_demo.service;

import com.repochka.product_demo.dto.ProductCreateRequest;
import com.repochka.product_demo.dto.ProductResponse;
import com.repochka.product_demo.dto.ProductUpdateRequest;
import com.repochka.product_demo.entity.Category;
import com.repochka.product_demo.entity.Product;
import com.repochka.product_demo.exception.ProductNotFoundException;
import com.repochka.product_demo.mapper.ProductMapper;
import com.repochka.product_demo.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct_shouldReturnResponseWithCorrectFields() {
        ProductCreateRequest request = new ProductCreateRequest(
                "Clean Code", "A handbook", new BigDecimal("39.99"), Category.BOOKS);

        Product entity = Product.builder()
                .name("Clean Code").description("A handbook")
                .price(new BigDecimal("39.99")).category(Category.BOOKS).build();

        Product saved = Product.builder()
                .id(1L).name("Clean Code").description("A handbook")
                .price(new BigDecimal("39.99")).category(Category.BOOKS)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();

        ProductResponse mappedResponse = ProductResponse.builder()
                .id(1L).name("Clean Code").description("A handbook")
                .price(new BigDecimal("39.99")).category(Category.BOOKS)
                .createdAt(saved.getCreatedAt()).updatedAt(saved.getUpdatedAt()).build();

        when(productMapper.toEntity(request)).thenReturn(entity);
        when(productRepository.save(entity)).thenReturn(saved);
        when(productMapper.toResponse(saved)).thenReturn(mappedResponse);

        ProductResponse result = productService.createProduct(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Clean Code");
        assertThat(result.getPrice()).isEqualByComparingTo("39.99");
        assertThat(result.getDiscountedPrice()).isEqualByComparingTo("35.99");
        assertThat(result.getCategory()).isEqualTo(Category.BOOKS);
    }

    @Test
    void getProductById_shouldReturnProduct() {
        Product product = Product.builder()
                .id(1L).name("Test").price(new BigDecimal("10.00"))
                .category(Category.ELECTRONICS)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();

        ProductResponse mappedResponse = ProductResponse.builder()
                .id(1L).name("Test").price(new BigDecimal("10.00"))
                .category(Category.ELECTRONICS).build();

        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(mappedResponse);

        ProductResponse result = productService.getProductById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test");
        assertThat(result.getDiscountedPrice()).isEqualByComparingTo("10.00");
    }

    @Test
    void getProductById_shouldThrowWhenNotFound() {
        when(productRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found with id: 99");
    }

    @Test
    void updateProduct_shouldPersistChangesAndReturnResponse() {
        Product existing = Product.builder()
                .id(1L).name("Old Name").price(new BigDecimal("10.00"))
                .category(Category.ELECTRONICS)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();

        ProductUpdateRequest request = new ProductUpdateRequest(
                "New Name", null, new BigDecimal("20.00"), Category.BOOKS);

        Product savedProduct = Product.builder()
                .id(1L).name("New Name").price(new BigDecimal("20.00"))
                .category(Category.BOOKS)
                .createdAt(existing.getCreatedAt()).updatedAt(OffsetDateTime.now()).build();

        ProductResponse mappedResponse = ProductResponse.builder()
                .id(1L).name("New Name").price(new BigDecimal("20.00"))
                .category(Category.BOOKS).build();

        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(savedProduct);
        when(productMapper.toResponse(savedProduct)).thenReturn(mappedResponse);

        ProductResponse result = productService.updateProduct(1L, request);

        verify(productMapper).updateEntityFromRequest(request, existing);
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getPrice()).isEqualByComparingTo("20.00");
    }

    @Test
    void updateProduct_shouldThrowWhenProductNotFound() {
        when(productRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        ProductUpdateRequest request = new ProductUpdateRequest(
                "Name", null, new BigDecimal("10.00"), Category.OTHER);

        assertThatThrownBy(() -> productService.updateProduct(99L, request))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void softDeleteProduct_shouldSetDeletedAndTimestamp() {
        Product product = Product.builder()
                .id(1L).name("Test").price(new BigDecimal("10.00"))
                .category(Category.FOOD).deleted(false).build();

        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.softDeleteProduct(1L);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());

        Product saved = captor.getValue();
        assertThat(saved.isDeleted()).isTrue();
        assertThat(saved.getDeletedAt()).isNotNull();
    }

    @Test
    void softDeleteProduct_shouldThrowWhenProductNotFound() {
        when(productRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.softDeleteProduct(99L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void listProducts_shouldReturnPaginatedResults() {
        Pageable pageable = PageRequest.of(0, 20);
        Product product = Product.builder()
                .id(1L).name("Test").price(new BigDecimal("10.00"))
                .category(Category.ELECTRONICS).build();
        Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

        ProductResponse mappedResponse = ProductResponse.builder()
                .id(1L).name("Test").price(new BigDecimal("10.00"))
                .category(Category.ELECTRONICS).build();

        when(productRepository.findAllByDeletedFalse(pageable)).thenReturn(productPage);
        when(productMapper.toResponse(product)).thenReturn(mappedResponse);

        Page<ProductResponse> result = productService.listProducts(pageable, null);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Test");
    }

    @Test
    void listProducts_shouldFilterByCategory() {
        Pageable pageable = PageRequest.of(0, 20);
        Product book = Product.builder()
                .id(1L).name("Clean Code").price(new BigDecimal("39.99"))
                .category(Category.BOOKS).build();
        Page<Product> productPage = new PageImpl<>(List.of(book), pageable, 1);

        ProductResponse mappedResponse = ProductResponse.builder()
                .id(1L).name("Clean Code").price(new BigDecimal("39.99"))
                .category(Category.BOOKS).build();

        when(productRepository.findAllByCategoryAndDeletedFalse(Category.BOOKS, pageable)).thenReturn(productPage);
        when(productMapper.toResponse(book)).thenReturn(mappedResponse);

        Page<ProductResponse> result = productService.listProducts(pageable, Category.BOOKS);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategory()).isEqualTo(Category.BOOKS);
        verify(productRepository).findAllByCategoryAndDeletedFalse(Category.BOOKS, pageable);
    }

    @Test
    void listProducts_shouldReturnEmptyPageWhenNoMatches() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(productRepository.findAllByCategoryAndDeletedFalse(Category.FOOD, pageable)).thenReturn(emptyPage);

        Page<ProductResponse> result = productService.listProducts(pageable, Category.FOOD);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void calculateDiscountedPrice_booksGets10PercentDiscount() {
        assertThat(productService.calculateDiscountedPrice(new BigDecimal("100.00"), Category.BOOKS))
                .isEqualByComparingTo("90.00");
    }

    @Test
    void calculateDiscountedPrice_electronicsNoDiscount() {
        assertThat(productService.calculateDiscountedPrice(new BigDecimal("200.00"), Category.ELECTRONICS))
                .isEqualByComparingTo("200.00");
    }

    @Test
    void calculateDiscountedPrice_foodNoDiscount() {
        assertThat(productService.calculateDiscountedPrice(new BigDecimal("50.00"), Category.FOOD))
                .isEqualByComparingTo("50.00");
    }

    @Test
    void calculateDiscountedPrice_otherNoDiscount() {
        assertThat(productService.calculateDiscountedPrice(new BigDecimal("75.00"), Category.OTHER))
                .isEqualByComparingTo("75.00");
    }

    @Test
    void calculateDiscountedPrice_booksRoundsHalfUp() {
        assertThat(productService.calculateDiscountedPrice(new BigDecimal("33.33"), Category.BOOKS))
                .isEqualByComparingTo("30.00");
    }

    @Test
    void getProductById_shouldReturnDiscountedPriceForBooks() {
        Product product = Product.builder()
                .id(1L).name("Book").price(new BigDecimal("100.00"))
                .category(Category.BOOKS)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();

        ProductResponse mappedResponse = ProductResponse.builder()
                .id(1L).name("Book").price(new BigDecimal("100.00"))
                .category(Category.BOOKS).build();

        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(mappedResponse);

        ProductResponse result = productService.getProductById(1L);

        assertThat(result.getDiscountedPrice()).isEqualByComparingTo("90.00");
    }
}
