package com.fonepay.devportal.modules.admin.developer.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.fonepay.devportal.common.constant.DeveloperConstants;
import com.fonepay.devportal.modules.admin.developer.dto.request.DeveloperSearchCriteriaDto;

public final class DeveloperQueryBuilder {

    private DeveloperQueryBuilder() {
    }

    public static Query buildQuery(DeveloperSearchCriteriaDto criteria) {
        Query query = new Query();
        List<Criteria> andCriteriaList = new ArrayList<>();

        // Enforce Developer Role Filter
        andCriteriaList.add(Criteria.where("roles.role_name").is(DeveloperConstants.DEVELOPER_ROLE));

        // Status filter if provided
        if (criteria.getStatus() != null) {
            andCriteriaList.add(Criteria.where("status").is(criteria.getStatus()));
        }

        // Search filter across fullName, email, and companyName
        String search = criteria.getSearch();
        if (search != null && !search.isBlank()) {
            String escapedPattern = Pattern.quote(search.trim());
            Criteria searchCriteria = new Criteria().orOperator(
                    Criteria.where("fullName").regex(escapedPattern, "i"),
                    Criteria.where("email").regex(escapedPattern, "i"),
                    Criteria.where("companyName").regex(escapedPattern, "i"));
            andCriteriaList.add(searchCriteria);
        }

        if (!andCriteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(andCriteriaList.toArray(new Criteria[0])));
        }

        return query;
    }
}
