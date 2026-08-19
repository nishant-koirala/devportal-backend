package com.smartinventorysystem.modules.category.service;

import com.smartinventorysystem.common.dto.PageResponse;
import com.smartinventorysystem.constants.MessageConstants;
import com.smartinventorysystem.exceptions.DuplicateCategoryException;
import com.smartinventorysystem.exceptions.ResourceNotFoundException;
import com.smartinventorysystem.modules.category.dto.request.CategoryFilterRequest;
import com.smartinventorysystem.modules.category.dto.request.CreateCategoryRequest;
import com.smartinventorysystem.modules.category.dto.request.UpdateCategoryRequest;
import com.smartinventorysystem.modules.category.dto.response.CategoryResponse;
import com.smartinventorysystem.modules.category.entity.Category;
import com.smartinventorysystem.modules.category.mapper.CategoryMapper;
import com.smartinventorysystem.modules.category.repository.CategoryRepository;
import com.smartinventorysystem.modules.category.specification.CategorySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final Clock clock;

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {

        if (categoryRepository.existsByCategoryName(request.getCategoryName())) {
            throw new DuplicateCategoryException("Category already exists with name: " + request.getCategoryName());
        }

        Category category = categoryMapper.toEntity(request);
        category.setCreatedAt(LocalDateTime.now(clock));

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Integer id, UpdateCategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.CATEGORY_NOT_FOUND));

        if (request.getCategoryName() != null && !request.getCategoryName().isBlank()) {

            if (categoryRepository.existsByCategoryNameAndCategoryIdNot(
                    request.getCategoryName(),
                    id
            )) {
                throw new DuplicateCategoryException(
                        "Category already exists with name: " + request.getCategoryName()
                );
            }

            category.setCategoryName(request.getCategoryName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        category.setUpdatedAt(LocalDateTime.now(clock));

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Integer id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.CATEGORY_NOT_FOUND));

        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Integer id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.CATEGORY_NOT_FOUND));

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryMapper.toResponseList(categoryRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> getCategories(CategoryFilterRequest request) {
        Pageable pageable = createPageable(request);
        Specification<Category> specification = CategorySpecification.withFilters(request);

        Page<Category> categoryPage = categoryRepository.findAll(specification, pageable);
        return PageResponse.of(categoryPage, categoryMapper::toResponse);
    }

    private Pageable createPageable(CategoryFilterRequest request) {
        int page = (request != null && request.getPage() != null) ? request.getPage() : 0;
        int size = (request != null && request.getSize() != null) ? request.getSize() : 10;

        String sortBy = (request != null && request.getSortBy() != null) ? request.getSortBy() : "createdAt";
        String sortDir = (request != null && request.getSortDir() != null) ? request.getSortDir() : "desc";

        String targetProperty = mapSortProperty(sortBy);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Sort sort = Sort.by(direction, targetProperty);
        return PageRequest.of(page, size, sort);
    }

    private String mapSortProperty(String sortBy) {
        if (sortBy == null) {
            return "createdAt";
        }
        return switch (sortBy.trim().toLowerCase()) {
            case "name", "categoryname" -> "categoryName";
            case "description" -> "description";
            case "date", "createdat" -> "createdAt";
            case "updatedat" -> "updatedAt";
            case "id", "categoryid" -> "categoryId";
            default -> "createdAt";
        };
    }
}