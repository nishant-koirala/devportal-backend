package com.fonepay.devportal.modules.user.document;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_products")
@IdClass(UserProduct.UserProductId.class)
public class UserProduct {

    @Id
    @Column(name = "user_id", length = 26, nullable = false)
    private String userId;

    @Id
    @Column(name = "product_id", length = 26, nullable = false, columnDefinition = "CHAR(26)")
    private String productId;

    @Column(name = "selected_at", nullable = false)
    private Instant selectedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProductId implements Serializable {

        private String userId;
        private String productId;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof UserProductId that)) {
                return false;
            }
            return Objects.equals(userId, that.userId) && Objects.equals(productId, that.productId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, productId);
        }
    }
}
