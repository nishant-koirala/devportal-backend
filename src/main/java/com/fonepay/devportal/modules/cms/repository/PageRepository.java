package com.fonepay.devportal.modules.cms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.fonepay.devportal.modules.cms.document.Page;
import com.fonepay.devportal.modules.cms.enums.PageStatus;

@Repository
public interface PageRepository extends MongoRepository<Page, String>, PageCustomRepository {

    @Query(value = "{ 'product_id': ?0, 'slug': ?1, 'status': 'PUBLISHED' }", fields = "{ 'draft_blocks': 0 }")
    Optional<Page> findPublishedByProductIdAndSlugExcludingDrafts(String productId, String slug);

    Optional<Page> findByProductIdAndSlugAndStatus(String productId, String slug, PageStatus status);

    List<Page> findByProductIdOrderByPageOrderAsc(String productId);

    List<Page> findByParentIdOrderByPageOrderAsc(String parentId);

    Optional<Page> findByProductIdAndSlug(String productId, String slug);

    List<Page> findByProductIdAndStatusNotOrderByPageOrderAsc(String productId, PageStatus status);

    List<Page> findByProductIdAndParentIdOrderByPageOrderAsc(String productId, String parentId);

    List<Page> findByProductIdAndParentIdIsNullOrderByPageOrderAsc(String productId);

    boolean existsByProductIdAndSlug(String productId, String slug);

    boolean existsByProductIdAndSlugAndIdNot(String productId, String slug, String id);
}
