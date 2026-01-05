package com.fastfood.order.application.service;

import com.fastfood.order.application.dto.MenuCategoryRequest;
import com.fastfood.order.application.dto.MenuCategoryResponse;
import com.fastfood.order.domain.entity.MenuCategory;
import com.fastfood.order.infrastructure.repository.MenuCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MenuCategoryService {

    private final MenuCategoryRepository categoryRepository;

    public MenuCategoryResponse createCategory(MenuCategoryRequest request) {
        log.info("Creating menu category: {}", request.getNameEn());
        
        MenuCategory category = MenuCategory.builder()
                .nameEn(request.getNameEn())
                .nameUr(request.getNameUr())
                .descriptionEn(request.getDescriptionEn())
                .descriptionUr(request.getDescriptionUr())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .imageUrl(request.getImageUrl())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        category = categoryRepository.save(category);
        return mapToResponse(category);
    }

    public MenuCategoryResponse updateCategory(Long id, MenuCategoryRequest request) {
        log.info("Updating menu category with ID: {}", id);
        
        MenuCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu category not found with ID: " + id));

        category.setNameEn(request.getNameEn());
        category.setNameUr(request.getNameUr());
        category.setDescriptionEn(request.getDescriptionEn());
        category.setDescriptionUr(request.getDescriptionUr());
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getImageUrl() != null) {
            category.setImageUrl(request.getImageUrl());
        }
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        category = categoryRepository.save(category);
        return mapToResponse(category);
    }

    @Transactional(readOnly = true)
    public MenuCategoryResponse getCategoryById(Long id) {
        MenuCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu category not found with ID: " + id));
        return mapToResponse(category);
    }

    /**
     * Get all categories, optionally filtered by active status
     * @param active Optional filter for active categories only
     * @return List of categories
     */
    @Transactional(readOnly = true)
    public List<MenuCategoryResponse> getAllCategories(Boolean active) {
        if (active != null && active) {
            return getActiveCategories();
        }
        return getAllCategories();
    }

    @Transactional(readOnly = true)
    public List<MenuCategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MenuCategoryResponse> getActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteCategory(Long id) {
        log.info("Deleting menu category with ID: {}", id);
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Menu category not found with ID: " + id);
        }
        categoryRepository.deleteById(id);
    }

    private MenuCategoryResponse mapToResponse(MenuCategory category) {
        return MenuCategoryResponse.builder()
                .id(category.getId())
                .nameEn(category.getNameEn())
                .nameUr(category.getNameUr())
                .descriptionEn(category.getDescriptionEn())
                .descriptionUr(category.getDescriptionUr())
                .displayOrder(category.getDisplayOrder())
                .imageUrl(category.getImageUrl())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}

