package com.fonepay.devportal.modules.cms.repository;

import com.fonepay.devportal.modules.cms.document.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends MongoRepository<Page, String> {
    
    // We can add the finder methods needed by Dev 2 here as well just to have them defined.
    List<Page> findByProductIdOrderByPageOrderAsc(String productId);
    List<Page> findByParentIdOrderByPageOrderAsc(String parentId);
    Optional<Page> findByProductIdAndSlug(String productId, String slug);
}
