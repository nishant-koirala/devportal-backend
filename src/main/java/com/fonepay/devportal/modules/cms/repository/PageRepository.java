package com.fonepay.devportal.modules.cms.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.fonepay.devportal.modules.cms.document.Page;
import com.fonepay.devportal.modules.cms.enums.PageStatus;

@Repository
public interface PageRepository extends MongoRepository<Page, String> {

    @Query(value = "{ 'product_id': ?0, 'slug': ?1, 'status': 'PUBLISHED' }", fields = "{ 'draft_blocks': 0 }")
    Optional<Page> findPublishedByProductIdAndSlugExcludingDrafts(String productId, String slug);

    Optional<Page> findByProductIdAndSlugAndStatus(String productId, String slug, PageStatus status);

    Optional<Page> findByProductIdAndSlug(String productId, String slug);
}
