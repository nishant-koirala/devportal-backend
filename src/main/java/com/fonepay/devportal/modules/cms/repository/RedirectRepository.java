package com.fonepay.devportal.modules.cms.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.fonepay.devportal.modules.cms.document.Redirect;
import java.util.Optional;

public interface RedirectRepository extends MongoRepository<Redirect, String> {
    Optional<Redirect> findByOldPath(String oldPath);
    void deleteByOldPath(String oldPath);
}
