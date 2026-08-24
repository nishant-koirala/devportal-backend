package com.fonepay.devportal.modules.admin.developer.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.DeveloperConstants;
import com.fonepay.devportal.common.dto.PageResponse;
import com.fonepay.devportal.common.exception.InvalidSortException;
import com.fonepay.devportal.modules.admin.developer.dto.request.DeveloperSearchCriteriaDto;
import com.fonepay.devportal.modules.admin.developer.dto.response.DeveloperResponseDto;
import com.fonepay.devportal.modules.admin.developer.mapper.DeveloperMapper;
import com.fonepay.devportal.modules.admin.developer.service.DeveloperManagementService;
import com.fonepay.devportal.modules.admin.developer.specification.DeveloperQueryBuilder;
import com.fonepay.devportal.modules.user.document.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeveloperManagementServiceImpl implements DeveloperManagementService {

    private final MongoTemplate mongoTemplate;
    private final DeveloperMapper developerMapper;

    @Override
    public PageResponse<DeveloperResponseDto> getDevelopers(DeveloperSearchCriteriaDto criteria) {
        log.info(
                "Fetching developers list with criteria: page={}, size={}, search='{}', status={}, sortBy='{}', sortDir='{}'",
                criteria.getPage(), criteria.getSize(), criteria.getSearch(), criteria.getStatus(),
                criteria.getSortBy(), criteria.getSortDirection());

        // Validate sorting parameters against whitelist
        validateSortCriteria(criteria.getSortBy(), criteria.getSortDirection());

        // Map sort field to document field and construct Pageable
        String mongoSortField = DeveloperConstants.SORT_FIELD_MAPPING.getOrDefault(criteria.getSortBy(), "createdAt");
        Sort.Direction direction = Sort.Direction.fromString(criteria.getSortDirection());
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), Sort.by(direction, mongoSortField));

        // Build dynamic MongoDB query
        Query query = DeveloperQueryBuilder.buildQuery(criteria);

        // Calculate total elements matching the query
        long totalElements = mongoTemplate.count(query, User.class);
        if (totalElements == 0) {
            log.info("No developers found matching the criteria");
            return PageResponse.empty(criteria.getPage(), criteria.getSize());
        }

        // Fetch paginated documents
        query.with(pageable);
        List<User> users = mongoTemplate.find(query, User.class);

        // Convert entities to DTOs
        List<DeveloperResponseDto> developerDtos = developerMapper.toDtoList(users);

        Page<DeveloperResponseDto> pageResult = new PageImpl<>(developerDtos, pageable, totalElements);
        log.info("Found {} developers, returning page {} of {}", totalElements, criteria.getPage(),
                pageResult.getTotalPages());

        return PageResponse.of(pageResult);
    }

    private void validateSortCriteria(String sortBy, String sortDirection) {
        if (sortBy != null && !sortBy.isBlank() && !DeveloperConstants.ALLOWED_SORT_FIELDS.contains(sortBy)) {
            log.warn("Invalid sort field requested: {}", sortBy);
            throw new InvalidSortException(DeveloperConstants.INVALID_SORT_FIELD_MESSAGE);
        }

        if (sortDirection != null && !sortDirection.equalsIgnoreCase("ASC")
                && !sortDirection.equalsIgnoreCase("DESC")) {
            log.warn("Invalid sort direction requested: {}", sortDirection);
            throw new InvalidSortException(DeveloperConstants.INVALID_SORT_DIRECTION_MESSAGE);
        }
    }
}
