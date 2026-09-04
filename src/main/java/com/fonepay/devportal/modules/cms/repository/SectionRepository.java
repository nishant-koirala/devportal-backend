package com.fonepay.devportal.modules.cms.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.fonepay.devportal.modules.cms.document.Section;
import java.util.Optional;

public interface SectionRepository extends MongoRepository<Section, String> {
    Optional<Section> findByProductIdAndSlug(String productId, String slug);
}
