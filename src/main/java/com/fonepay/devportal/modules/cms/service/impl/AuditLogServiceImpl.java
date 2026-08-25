package com.fonepay.devportal.modules.cms.service.impl;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fonepay.devportal.common.dto.PageResponse;
import com.fonepay.devportal.common.util.HttpRequestUtil;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.cms.document.AuditLog;
import com.fonepay.devportal.modules.cms.dto.request.AuditLogSearchCriteriaDto;
import com.fonepay.devportal.modules.cms.dto.response.AuditLogResponseDto;
import com.fonepay.devportal.modules.cms.mapper.AuditLogMapper;
import com.fonepay.devportal.modules.cms.repository.AuditLogRepository;
import com.fonepay.devportal.modules.cms.service.AuditLogService;
import com.fonepay.devportal.modules.user.document.User;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger FALLBACK_LOGGER = LoggerFactory.getLogger("AUDIT_FALLBACK_LOGGER");

    private final AuditLogRepository auditLogRepository;
    private final MongoTemplate mongoTemplate;
    private final AuditLogMapper auditLogMapper;
    private final Clock clock;

    @Override
    public AuditLog logAction(String adminId, String action, String targetId, String targetType, String sourceIp) {
        String resolvedAdminId = (adminId != null && !adminId.isBlank()) ? adminId : resolveCurrentAdminId();
        String resolvedSourceIp = (sourceIp != null && !sourceIp.isBlank()) ? sourceIp : resolveCurrentSourceIp();

        AuditLog auditLog = AuditLog.builder()
                .id(IdGenerator.nextUlid())
                .adminId(resolvedAdminId)
                .action(action != null ? action.trim().toUpperCase() : "UNKNOWN_ACTION")
                .targetId(targetId)
                .targetType(targetType != null ? targetType.trim().toUpperCase() : "UNKNOWN")
                .sourceIp(resolvedSourceIp)
                .timestamp(clock.instant())
                .build();

        try {
            AuditLog saved = auditLogRepository.save(auditLog);
            log.info("Audit log recorded: id={}, adminId={}, action={}, targetId={}, targetType={}, sourceIp={}",
                    saved.getId(), saved.getAdminId(), saved.getAction(), saved.getTargetId(), saved.getTargetType(), saved.getSourceIp());
            return saved;
        } catch (Exception ex) {
            FALLBACK_LOGGER.error("AUDIT_FALLBACK_LOG: Failed to persist audit log to MongoDB. Payload: [id={}, adminId={}, action={}, targetId={}, targetType={}, sourceIp={}, timestamp={}]. Reason: {}",
                    auditLog.getId(), auditLog.getAdminId(), auditLog.getAction(), auditLog.getTargetId(),
                    auditLog.getTargetType(), auditLog.getSourceIp(), auditLog.getTimestamp(), ex.getMessage(), ex);
            return auditLog;
        }
    }

    @Override
    public AuditLog logAction(String action, String targetId, String targetType) {
        return logAction(resolveCurrentAdminId(), action, targetId, targetType, resolveCurrentSourceIp());
    }

    @Override
    public PageResponse<AuditLogResponseDto> getAuditLogs(AuditLogSearchCriteriaDto criteria) {
        int page = Math.max(0, criteria.getPage());
        int size = criteria.getSize() > 0 ? criteria.getSize() : 20;
        Sort.Direction direction = "asc".equalsIgnoreCase(criteria.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortBy = (criteria.getSortBy() != null && !criteria.getSortBy().isBlank()) ? criteria.getSortBy() : "timestamp";

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        if (criteria.getAdminId() != null && !criteria.getAdminId().isBlank()) {
            criteriaList.add(Criteria.where("admin_id").is(criteria.getAdminId().trim()));
        }

        if (criteria.getTargetId() != null && !criteria.getTargetId().isBlank()) {
            criteriaList.add(Criteria.where("target_id").is(criteria.getTargetId().trim()));
        }

        if (criteria.getTargetType() != null && !criteria.getTargetType().isBlank()) {
            criteriaList.add(Criteria.where("target_type").is(criteria.getTargetType().trim().toUpperCase()));
        }

        if (criteria.getAction() != null && !criteria.getAction().isBlank()) {
            criteriaList.add(Criteria.where("action").is(criteria.getAction().trim().toUpperCase()));
        }

        if (criteria.getStartDate() != null && criteria.getEndDate() != null) {
            criteriaList.add(Criteria.where("timestamp").gte(criteria.getStartDate()).lte(criteria.getEndDate()));
        } else if (criteria.getStartDate() != null) {
            criteriaList.add(Criteria.where("timestamp").gte(criteria.getStartDate()));
        } else if (criteria.getEndDate() != null) {
            criteriaList.add(Criteria.where("timestamp").lte(criteria.getEndDate()));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long totalElements = mongoTemplate.count(query, AuditLog.class);
        query.with(pageable);
        List<AuditLog> auditLogs = mongoTemplate.find(query, AuditLog.class);

        List<AuditLogResponseDto> content = auditLogMapper.toResponseDtoList(auditLogs);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return PageResponse.<AuditLogResponseDto>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .isFirst(page == 0)
                .isLast(page >= totalPages - 1)
                .isEmpty(content.isEmpty())
                .build();
    }


    private String resolveCurrentAdminId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof User user) {
                    return user.getUserId();
                }
                if (principal instanceof String str && !"anonymousUser".equalsIgnoreCase(str)) {
                    return str;
                }
                if (authentication.getName() != null && !"anonymousUser".equalsIgnoreCase(authentication.getName())) {
                    return authentication.getName();
                }
            }
        } catch (Exception e) {
            log.debug("Could not resolve admin ID from SecurityContext: {}", e.getMessage());
        }
        return "SYSTEM";
    }

    private String resolveCurrentSourceIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return HttpRequestUtil.getClientIp(request);
            }
        } catch (Exception e) {
            log.debug("Could not resolve client IP from request: {}", e.getMessage());
        }
        return null;
    }
}
