package com.fonepay.devportal.modules.admin.developer.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fonepay.devportal.common.constant.DeveloperConstants;
import com.fonepay.devportal.common.dto.PageResponse;
import com.fonepay.devportal.common.exception.InvalidSortException;
import com.fonepay.devportal.modules.admin.developer.dto.request.DeveloperSearchCriteriaDto;
import com.fonepay.devportal.modules.admin.developer.dto.response.DeveloperResponseDto;
import com.fonepay.devportal.modules.admin.developer.mapper.DeveloperMapper;
import com.fonepay.devportal.modules.admin.developer.service.DeveloperManagementService;
import com.fonepay.devportal.modules.admin.developer.specification.DeveloperQueryBuilder;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.modules.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeveloperManagementServiceImpl implements DeveloperManagementService {

    private final UserRepository userRepository;
    private final DeveloperMapper developerMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeveloperResponseDto> getDevelopers(DeveloperSearchCriteriaDto criteria) {
        log.info(
                "Fetching developers list with criteria: page={}, size={}, search='{}', status={}, sortBy='{}', sortDir='{}'",
                criteria.getPage(), criteria.getSize(), criteria.getSearch(), criteria.getStatus(),
                criteria.getSortBy(), criteria.getSortDirection());

        validateSortCriteria(criteria.getSortBy(), criteria.getSortDirection());

        String sortField = DeveloperConstants.SORT_FIELD_MAPPING.getOrDefault(criteria.getSortBy(), "createdAt");
        Sort.Direction direction = Sort.Direction.fromString(criteria.getSortDirection());
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), Sort.by(direction, sortField));

        Page<User> users = userRepository.findAll(DeveloperQueryBuilder.buildQuery(criteria), pageable);
        if (users.isEmpty()) {
            log.info("No developers found matching the criteria");
            return PageResponse.empty(criteria.getPage(), criteria.getSize());
        }

        List<DeveloperResponseDto> developerDtos = developerMapper.toDtoList(users.getContent());
        log.info("Found {} developers, returning page {} of {}", users.getTotalElements(), criteria.getPage(),
                users.getTotalPages());

        return PageResponse.of(users, developerDtos);
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
