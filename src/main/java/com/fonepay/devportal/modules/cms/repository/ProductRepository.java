package com.fonepay.devportal.modules.cms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.fonepay.devportal.modules.cms.document.Product;
import com.fonepay.devportal.modules.cms.enums.ProductStatus;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    Optional<Product> findBySlug(String slug);

    Optional<Product> findByName(String name);

    Optional<Product> findBySlugAndStatus(String slug, ProductStatus status);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    boolean existsBySlugAndIdNot(String slug, String id);

    boolean existsByNameAndIdNot(String name, String id);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    List<Product> findByStatusOrderByDisplayOrderAsc(ProductStatus status);

    Page<Product> findByNameContainingIgnoreCaseOrSlugContainingIgnoreCase(String name, String slug, Pageable pageable);

    Page<Product> findByStatusAndNameContainingIgnoreCase(ProductStatus status, String name, Pageable pageable);
}
