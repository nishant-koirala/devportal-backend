package com.fonepay.devportal.modules.cms.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.fonepay.devportal.modules.cms.document.Revision;

import java.util.List;
import java.util.Optional;

public interface RevisionRepository extends MongoRepository<Revision, String> {
    List<Revision> findByPageIdOrderByVersionDesc(String pageId);
    Optional<Revision> findByPageIdAndVersion(String pageId, int version);
    Revision findTopByPageIdOrderByVersionDesc(String pageId);
}
