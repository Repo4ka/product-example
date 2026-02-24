package com.repochka.product_demo.repository;

import com.repochka.product_demo.entity.Category;
import com.repochka.product_demo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.deleted = false")
    Optional<Product> findByIdAndDeletedFalse(@Param("id") Long id);

    @Query("SELECT p FROM Product p WHERE p.deleted = false")
    Page<Product> findAllByDeletedFalse(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.deleted = false")
    Page<Product> findAllByCategoryAndDeletedFalse(@Param("category") Category category, Pageable pageable);
}
