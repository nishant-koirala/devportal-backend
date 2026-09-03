package com.fonepay.devportal.modules.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.fonepay.devportal.modules.product.document.ProductVersion;
import com.fonepay.devportal.modules.product.enums.ProductVersionStatus;

@Repository
public interface ProductVersionRepository extends MongoRepository<ProductVersion, String> {

    List<ProductVersion> findByProductId(String productId);

    List<ProductVersion> findByProductIdOrderByDisplayOrderAsc(String productId);

    List<ProductVersion> findByProductIdAndStatusOrderByDisplayOrderAsc(String productId, ProductVersionStatus status);

    Optional<ProductVersion> findByProductIdAndSlug(String productId, String slug);

    Optional<ProductVersion> findByProductIdAndVersionName(String productId, String versionName);

    Optional<ProductVersion> findByProductIdAndIsLatestTrue(String productId);

    Optional<ProductVersion> findByProductIdAndIsDefaultTrue(String productId);

    Optional<ProductVersion> findByProductSlugAndSlug(String productSlug, String slug);

    Optional<ProductVersion> findByProductSlugAndIsLatestTrue(String productSlug);

    List<ProductVersion> findByProductSlugOrderByDisplayOrderAsc(String productSlug);

    boolean existsByProductIdAndSlug(String productId, String slug);

    boolean existsByProductIdAndVersionName(String productId, String versionName);

    boolean existsByProductIdAndSlugAndIdNot(String productId, String slug, String id);

    Page<ProductVersion> findByProductId(String productId, Pageable pageable);

    @Query(value = "{ 'product_id': ?0, 'slug': ?1 }", fields = "{ 'page_snapshots': 0 }")
    Optional<ProductVersion> findSummaryByProductIdAndSlug(String productId, String slug);

    @Query(value = "{ 'product_id': ?0 }", fields = "{ 'page_snapshots': 0 }")
    List<ProductVersion> findSummariesByProductIdOrderByDisplayOrderAsc(String productId);

    @Query(value = "{ 'product_slug': ?0, 'slug': ?1 }", fields = "{ 'page_snapshots': 0 }")
    Optional<ProductVersion> findSummaryByProductSlugAndSlug(String productSlug, String slug);

    @Query(value = "{ 'product_slug': ?0, 'is_latest': true }", fields = "{ 'page_snapshots': 0 }")
    Optional<ProductVersion> findSummaryByProductSlugAndIsLatestTrue(String productSlug);
}
