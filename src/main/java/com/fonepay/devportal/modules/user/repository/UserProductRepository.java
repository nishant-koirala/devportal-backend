package com.fonepay.devportal.modules.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fonepay.devportal.modules.user.document.UserProduct;

@Repository
public interface UserProductRepository extends JpaRepository<UserProduct, UserProduct.UserProductId> {

    List<UserProduct> findByUserId(String userId);

    Optional<UserProduct> findByUserIdAndProductId(String userId, String productId);

    boolean existsByUserIdAndProductId(String userId, String productId);

    void deleteByUserIdAndProductId(String userId, String productId);
}
