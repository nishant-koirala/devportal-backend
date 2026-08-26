package com.fonepay.devportal.modules.cms.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.dto.PageResponse;
import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.DuplicateResourceException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.cms.document.Product;
import com.fonepay.devportal.modules.cms.document.ProductResource;
import com.fonepay.devportal.modules.cms.dto.request.CreateProductRequest;
import com.fonepay.devportal.modules.cms.dto.request.CreateProductResourceRequest;
import com.fonepay.devportal.modules.cms.dto.request.ProductSearchCriteriaDto;
import com.fonepay.devportal.modules.cms.dto.request.RejectProductRequest;
import com.fonepay.devportal.modules.cms.dto.request.UpdateProductRequest;
import com.fonepay.devportal.modules.cms.dto.request.UpdateProductResourceRequest;
import com.fonepay.devportal.modules.cms.dto.request.UpdateProductStatusRequest;
import com.fonepay.devportal.modules.cms.dto.response.ProductDetailResponseDto;
import com.fonepay.devportal.modules.cms.dto.response.ProductResourceResponseDto;
import com.fonepay.devportal.modules.cms.dto.response.ProductResponseDto;
import com.fonepay.devportal.modules.cms.enums.ProductStatus;
import com.fonepay.devportal.modules.cms.mapper.ProductMapper;
import com.fonepay.devportal.modules.cms.repository.ProductRepository;
import com.fonepay.devportal.modules.cms.service.AuditLogService;
import com.fonepay.devportal.modules.cms.service.ProductService;
import com.fonepay.devportal.modules.cms.service.PublishService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    private final ProductRepository productRepository;
    private final MongoTemplate mongoTemplate;
    private final ProductMapper productMapper;
    private final AuditLogService auditLogService;
    private final PublishService publishService;
    private final Clock clock;

    @Override
    public ProductDetailResponseDto createProduct(CreateProductRequest request, String adminId, String sourceIp) {
        validateCreateProductRequest(request);

        String slug = request.getSlug().trim().toLowerCase();
        String name = request.getName().trim();

        if (productRepository.existsBySlug(slug)) {
            throw new DuplicateResourceException("Product with slug '" + slug + "' already exists");
        }
        if (productRepository.existsByName(name)) {
            throw new DuplicateResourceException("Product with name '" + name + "' already exists");
        }

        Instant now = clock.instant();
        ProductStatus status = request.getStatus() != null ? request.getStatus() : ProductStatus.DRAFT;

        List<ProductResource> resources = new ArrayList<>();
        if (request.getResources() != null) {
            for (CreateProductResourceRequest resReq : request.getResources()) {
                validateResourceRequest(resReq.getName(), resReq.getUrl());
                ProductResource resource = productMapper.toResource(resReq);
                resource.setResourceId(IdGenerator.nextUlid());
                resources.add(resource);
            }
        }

        Product product = Product.builder()
                .id(IdGenerator.nextUlid())
                .name(name)
                .slug(slug)
                .shortDescription(request.getShortDescription() != null ? request.getShortDescription().trim() : null)
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .logoUrl(request.getLogoUrl() != null ? request.getLogoUrl().trim() : null)
                .status(status)
                .displayOrder(request.getDisplayOrder())
                .resources(resources)
                .createdAt(now)
                .updatedAt(now)
                .createdBy(adminId)
                .build();

        Product saved = saveWithOptimisticLockHandling(product);
        log.info("Product created: id={}, slug={}, name={}, by admin={}", saved.getId(), saved.getSlug(), saved.getName(), adminId);

        auditLogService.logAction(adminId, "CREATE_PRODUCT", saved.getId(), "PRODUCT", sourceIp);

        return productMapper.toDetailResponseDto(saved);
    }

    @Override
    public ProductDetailResponseDto getProductById(String id) {
        if (id == null || id.isBlank()) {
            throw new BadRequestException("Product ID must not be blank");
        }
        Product product = productRepository.findById(id.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return productMapper.toDetailResponseDto(product);
    }

    @Override
    public ProductDetailResponseDto getProductBySlug(String slug) {
        validateSlugFormat(slug);
        Product product = productRepository.findBySlug(slug.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
        return productMapper.toDetailResponseDto(product);
    }

    @Override
    public ProductDetailResponseDto updateProduct(String id, UpdateProductRequest request, String adminId, String sourceIp) {
        if (id == null || id.isBlank()) {
            throw new BadRequestException("Product ID must not be blank");
        }
        validateUpdateProductRequest(request);

        Product product = productRepository.findById(id.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        String slug = request.getSlug().trim().toLowerCase();
        String name = request.getName().trim();

        if (productRepository.existsBySlugAndIdNot(slug, id.trim())) {
            throw new DuplicateResourceException("Product with slug '" + slug + "' already exists");
        }
        if (productRepository.existsByNameAndIdNot(name, id.trim())) {
            throw new DuplicateResourceException("Product with name '" + name + "' already exists");
        }

        if (request.getVersion() != null && !request.getVersion().equals(product.getVersion())) {
            throw new DuplicateResourceException("Concurrent modification detected: Product version conflict. Please reload and retry.");
        }

        product.setName(name);
        product.setSlug(slug);
        product.setShortDescription(request.getShortDescription() != null ? request.getShortDescription().trim() : null);
        product.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        product.setLogoUrl(request.getLogoUrl() != null ? request.getLogoUrl().trim() : null);
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }
        product.setDisplayOrder(request.getDisplayOrder());
        product.setUpdatedAt(clock.instant());

        Product saved = saveWithOptimisticLockHandling(product);
        log.info("Product updated: id={}, slug={}, by admin={}", saved.getId(), saved.getSlug(), adminId);

        auditLogService.logAction(adminId, "UPDATE_PRODUCT", saved.getId(), "PRODUCT", sourceIp);

        return productMapper.toDetailResponseDto(saved);
    }

    @Override
    public ProductDetailResponseDto updateProductStatus(String id, UpdateProductStatusRequest request, String adminId, String sourceIp) {
        if (id == null || id.isBlank()) {
            throw new BadRequestException("Product ID must not be blank");
        }
        if (request.getStatus() == null) {
            throw new BadRequestException("Status must not be null");
        }

        Product product = productRepository.findById(id.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        ProductStatus oldStatus = product.getStatus();
        product.setStatus(request.getStatus());
        product.setUpdatedAt(clock.instant());

        Product saved = saveWithOptimisticLockHandling(product);
        log.info("Product status updated: id={}, oldStatus={}, newStatus={}, by admin={}", saved.getId(), oldStatus, saved.getStatus(), adminId);

        auditLogService.logAction(adminId, "UPDATE_PRODUCT_STATUS", saved.getId(), "PRODUCT", sourceIp);

        return productMapper.toDetailResponseDto(saved);
    }

    @Override
    public ProductDetailResponseDto submitForReview(String id, String userId, String sourceIp) {
        if (id == null || id.isBlank()) {
            throw new BadRequestException("Product ID or slug must not be blank");
        }

        Product product = findProductByIdOrSlug(id.trim());

        if (product.getStatus() == ProductStatus.PUBLISHED) {
            throw new BadRequestException("Cannot submit a product that is already PUBLISHED");
        }
        if (product.getStatus() == ProductStatus.DEPRECATED) {
            throw new BadRequestException("Cannot submit a DEPRECATED product for review");
        }
        if (product.getStatus() == ProductStatus.IN_REVIEW) {
            throw new BadRequestException("Product is already IN_REVIEW");
        }

        // Validate basic product completeness before submitting
        if (product.getName() == null || product.getName().isBlank()) {
            throw new BadRequestException("Product must have a valid name before submitting for review");
        }
        if (product.getSlug() == null || product.getSlug().isBlank()) {
            throw new BadRequestException("Product must have a valid slug before submitting for review");
        }

        Instant now = clock.instant();
        product.setStatus(ProductStatus.IN_REVIEW);
        product.setSubmittedBy(userId);
        product.setSubmittedAt(now);
        product.setReviewNotes(null); // Clear previous review notes on resubmission
        product.setUpdatedAt(now);

        Product saved = saveWithOptimisticLockHandling(product);
        log.info("Product submitted for review: id={}, slug={}, submittedBy={}", saved.getId(), saved.getSlug(), userId);

        auditLogService.logAction(userId, "PRODUCT_SUBMIT_REVIEW", saved.getId(), "PRODUCT", sourceIp);

        return productMapper.toDetailResponseDto(saved);
    }

    @Override
    public ProductDetailResponseDto approveProduct(String id, String adminId, String sourceIp) {
        if (id == null || id.isBlank()) {
            throw new BadRequestException("Product ID or slug must not be blank");
        }

        Product product = findProductByIdOrSlug(id.trim());

        if (product.getStatus() == ProductStatus.PUBLISHED) {
            throw new BadRequestException("Product is already PUBLISHED");
        }
        if (product.getStatus() != ProductStatus.IN_REVIEW) {
            throw new BadRequestException("Only products with IN_REVIEW status can be approved. Current status: " + product.getStatus());
        }

        Instant now = clock.instant();
        product.setStatus(ProductStatus.PUBLISHED);
        product.setReviewedBy(adminId);
        product.setReviewedAt(now);
        product.setUpdatedAt(now);

        Product saved = saveWithOptimisticLockHandling(product);
        log.info("Product approved & published: id={}, slug={}, approvedBy={}", saved.getId(), saved.getSlug(), adminId);

        // Auto-publish all existing unarchived pages under this product and create their initial PageVersion (v1)
        publishService.publishPagesForProduct(saved.getId(), adminId, sourceIp, "Auto-published upon product approval");

        auditLogService.logAction(adminId, "PRODUCT_APPROVE_PUBLISH", saved.getId(), "PRODUCT", sourceIp);

        return productMapper.toDetailResponseDto(saved);
    }

    @Override
    public ProductDetailResponseDto rejectProduct(String id, RejectProductRequest request, String adminId, String sourceIp) {
        if (id == null || id.isBlank()) {
            throw new BadRequestException("Product ID or slug must not be blank");
        }
        if (request == null || request.getReason() == null || request.getReason().isBlank()) {
            throw new BadRequestException("Rejection reason / review notes are required");
        }

        Product product = findProductByIdOrSlug(id.trim());

        if (product.getStatus() != ProductStatus.IN_REVIEW) {
            throw new BadRequestException("Only products with IN_REVIEW status can be rejected. Current status: " + product.getStatus());
        }

        Instant now = clock.instant();
        product.setStatus(ProductStatus.DRAFT);
        product.setReviewNotes(request.getReason().trim());
        product.setReviewedBy(adminId);
        product.setReviewedAt(now);
        product.setUpdatedAt(now);

        Product saved = saveWithOptimisticLockHandling(product);
        log.info("Product rejected: id={}, slug={}, rejectedBy={}, reason={}", saved.getId(), saved.getSlug(), adminId, request.getReason());

        auditLogService.logAction(adminId, "PRODUCT_REJECT", saved.getId(), "PRODUCT", sourceIp);

        return productMapper.toDetailResponseDto(saved);
    }

    @Override
    public void deleteProduct(String id, String adminId, String sourceIp) {
        if (id == null || id.isBlank()) {
            throw new BadRequestException("Product ID must not be blank");
        }

        Product product = productRepository.findById(id.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        productRepository.delete(product);
        log.info("Product deleted: id={}, slug={}, by admin={}", id, product.getSlug(), adminId);

        auditLogService.logAction(adminId, "DELETE_PRODUCT", id.trim(), "PRODUCT", sourceIp);
    }

    @Override
    public PageResponse<ProductResponseDto> getProducts(ProductSearchCriteriaDto criteria) {
        int page = Math.max(0, criteria.getPage());
        int size = criteria.getSize() > 0 ? criteria.getSize() : 10;
        Sort.Direction direction = "desc".equalsIgnoreCase(criteria.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortBy = (criteria.getSortBy() != null && !criteria.getSortBy().isBlank()) ? criteria.getSortBy() : "displayOrder";

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        if (criteria.getStatus() != null) {
            criteriaList.add(Criteria.where("status").is(criteria.getStatus()));
        }

        if (criteria.getSearch() != null && !criteria.getSearch().isBlank()) {
            String term = criteria.getSearch().trim();
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("name").regex(Pattern.quote(term), "i"),
                    Criteria.where("slug").regex(Pattern.quote(term), "i")
            ));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long totalElements = mongoTemplate.count(query, Product.class);
        query.with(pageable);
        List<Product> products = mongoTemplate.find(query, Product.class);

        List<ProductResponseDto> content = productMapper.toResponseDtoList(products);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return PageResponse.<ProductResponseDto>builder()
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


    // Embedded ProductResource operations

    @Override
    public ProductDetailResponseDto addResource(String productId, CreateProductResourceRequest request, String adminId, String sourceIp) {
        if (productId == null || productId.isBlank()) {
            throw new BadRequestException("Product ID must not be blank");
        }
        validateResourceRequest(request.getName(), request.getUrl());

        Product product = productRepository.findById(productId.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (product.getResources() == null) {
            product.setResources(new ArrayList<>());
        }

        ProductResource resource = productMapper.toResource(request);
        resource.setResourceId(IdGenerator.nextUlid());
        product.getResources().add(resource);
        product.setUpdatedAt(clock.instant());

        Product saved = saveWithOptimisticLockHandling(product);
        log.info("Resource added to product: productId={}, resourceId={}, by admin={}", productId, resource.getResourceId(), adminId);

        auditLogService.logAction(adminId, "ADD_PRODUCT_RESOURCE", resource.getResourceId(), "PRODUCT_RESOURCE", sourceIp);

        return productMapper.toDetailResponseDto(saved);
    }

    @Override
    public ProductDetailResponseDto updateResource(String productId, String resourceId, UpdateProductResourceRequest request, String adminId, String sourceIp) {
        if (productId == null || productId.isBlank()) {
            throw new BadRequestException("Product ID must not be blank");
        }
        if (resourceId == null || resourceId.isBlank()) {
            throw new BadRequestException("Resource ID must not be blank");
        }
        validateResourceRequest(request.getName(), request.getUrl());

        Product product = productRepository.findById(productId.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (product.getResources() == null || product.getResources().isEmpty()) {
            throw new ResourceNotFoundException("Resource not found with ID: " + resourceId);
        }

        ProductResource targetResource = product.getResources().stream()
                .filter(r -> resourceId.trim().equals(r.getResourceId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + resourceId));

        targetResource.setName(request.getName().trim());
        targetResource.setResourceType(request.getResourceType().trim());
        targetResource.setUrl(request.getUrl().trim());
        targetResource.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) {
            targetResource.setActive(Boolean.TRUE.equals(request.getIsActive()));
        }


        product.setUpdatedAt(clock.instant());

        Product saved = saveWithOptimisticLockHandling(product);
        log.info("Resource updated: productId={}, resourceId={}, by admin={}", productId, resourceId, adminId);

        auditLogService.logAction(adminId, "UPDATE_PRODUCT_RESOURCE", resourceId.trim(), "PRODUCT_RESOURCE", sourceIp);

        return productMapper.toDetailResponseDto(saved);
    }

    @Override
    public ProductDetailResponseDto deleteResource(String productId, String resourceId, String adminId, String sourceIp) {
        if (productId == null || productId.isBlank()) {
            throw new BadRequestException("Product ID must not be blank");
        }
        if (resourceId == null || resourceId.isBlank()) {
            throw new BadRequestException("Resource ID must not be blank");
        }

        Product product = productRepository.findById(productId.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (product.getResources() == null || product.getResources().isEmpty()) {
            throw new ResourceNotFoundException("Resource not found with ID: " + resourceId);
        }

        boolean removed = product.getResources().removeIf(r -> resourceId.trim().equals(r.getResourceId()));
        if (!removed) {
            throw new ResourceNotFoundException("Resource not found with ID: " + resourceId);
        }

        product.setUpdatedAt(clock.instant());

        Product saved = saveWithOptimisticLockHandling(product);
        log.info("Resource removed from product: productId={}, resourceId={}, by admin={}", productId, resourceId, adminId);

        auditLogService.logAction(adminId, "DELETE_PRODUCT_RESOURCE", resourceId.trim(), "PRODUCT_RESOURCE", sourceIp);

        return productMapper.toDetailResponseDto(saved);
    }

    @Override
    public List<ProductResourceResponseDto> getResources(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new BadRequestException("Product ID must not be blank");
        }
        Product product = productRepository.findById(productId.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        return productMapper.toResourceResponseDtoList(product.getResources());
    }

    private Product saveWithOptimisticLockHandling(Product product) {
        try {
            return productRepository.save(product);
        } catch (OptimisticLockingFailureException ex) {
            log.warn("Optimistic lock failure while updating product ID: {}", product.getId());
            throw new DuplicateResourceException("Concurrent modification conflict: The product was updated by another administrator. Please reload and retry.");
        }
    }

    private void validateCreateProductRequest(CreateProductRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body must not be null");
        }
        validateSlugFormat(request.getSlug());
        if (request.getName() == null || request.getName().trim().length() < 2 || request.getName().trim().length() > 100) {
            throw new BadRequestException("Product name must be between 2 and 100 characters");
        }
    }

    private void validateUpdateProductRequest(UpdateProductRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body must not be null");
        }
        validateSlugFormat(request.getSlug());
        if (request.getName() == null || request.getName().trim().length() < 2 || request.getName().trim().length() > 100) {
            throw new BadRequestException("Product name must be between 2 and 100 characters");
        }
    }

    private void validateResourceRequest(String name, String url) {
        if (name == null || name.trim().isBlank() || name.trim().length() > 100) {
            throw new BadRequestException("Resource name must not be blank and must not exceed 100 characters");
        }
        if (url == null || url.trim().isBlank() || !url.trim().toLowerCase().matches("^https?://.*$")) {
            throw new BadRequestException("Resource URL must be a valid HTTP or HTTPS URL");
        }
    }

    private void validateSlugFormat(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new BadRequestException("Slug must not be blank");
        }
        String trimmed = slug.trim().toLowerCase();
        if (trimmed.length() < 2 || trimmed.length() > 100 || !SLUG_PATTERN.matcher(trimmed).matches()) {
            throw new BadRequestException("Invalid slug format. Slug must be 2-100 alphanumeric characters or hyphens (e.g. 'fpi-gateway')");
        }
    }

    private Product findProductByIdOrSlug(String idOrSlug) {
        if (idOrSlug == null || idOrSlug.isBlank()) {
            throw new BadRequestException("Product ID or slug must not be blank");
        }
        return productRepository.findById(idOrSlug.trim())
                .or(() -> productRepository.findBySlug(idOrSlug.trim().toLowerCase()))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID or slug: " + idOrSlug));
    }
}
