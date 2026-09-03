package com.fonepay.devportal.modules.admin.developer.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.fonepay.devportal.common.constant.DeveloperConstants;
import com.fonepay.devportal.modules.admin.developer.dto.request.DeveloperSearchCriteriaDto;
import com.fonepay.devportal.modules.user.document.Role;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.modules.user.document.UserRole;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public final class DeveloperQueryBuilder {

    private DeveloperQueryBuilder() {
    }

    public static Specification<User> buildQuery(DeveloperSearchCriteriaDto criteria) {
        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();
            Join<User, UserRole> userRoleJoin = root.join("roles", JoinType.INNER);
            Join<UserRole, Role> roleJoin = userRoleJoin.join("role", JoinType.INNER);
            predicates.add(cb.equal(roleJoin.get("roleName"), DeveloperConstants.DEVELOPER_ROLE));

            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }

            String search = criteria.getSearch();
            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(cb.coalesce(root.get("fullName"), "")), like),
                        cb.like(cb.lower(root.get("email")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("companyName"), "")), like)));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
