package com.fonepay.devportal.modules.notification.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.domain.Specification;

import com.fonepay.devportal.modules.notification.document.Broadcast;
import com.fonepay.devportal.modules.notification.dto.request.BroadcastFilterRequest;
import com.fonepay.devportal.modules.notification.enums.BroadcastDisplayMode;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

public final class BroadcastQueryBuilder {

    private BroadcastQueryBuilder() {
    }

    public static Specification<Broadcast> fromFilter(BroadcastFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                String like = "%" + filter.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("message")), like)));
            }

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getTargetRole() != null) {
                predicates.add(cb.equal(root.get("targetRole"), filter.getTargetRole()));
            }

            if (filter.getDisplayMode() != null) {
                if (query != null) {
                    query.distinct(true);
                }
                Join<Broadcast, BroadcastDisplayMode> modes = root.join("displayModes");
                predicates.add(cb.equal(modes, filter.getDisplayMode()));
            }

            if (filter.getPriority() != null) {
                predicates.add(cb.equal(root.get("priority"), filter.getPriority()));
            }

            if (filter.getCategory() != null) {
                predicates.add(cb.equal(root.get("category"), filter.getCategory()));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Set<String> allowedSortFields() {
        return Set.of("createdAt", "updatedAt", "startsAt", "expiresAt", "title", "priority", "status");
    }
}
