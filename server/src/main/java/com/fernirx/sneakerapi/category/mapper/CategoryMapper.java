package com.fernirx.sneakerapi.category.mapper;

import com.fernirx.sneakerapi.category.dto.request.UpdateCategoryRequest;
import com.fernirx.sneakerapi.category.dto.response.CategoryInternalResponse;
import com.fernirx.sneakerapi.category.dto.response.CategoryResponse;
import com.fernirx.sneakerapi.category.dto.response.TranslationResponse;
import com.fernirx.sneakerapi.category.entity.Category;
import com.fernirx.sneakerapi.category.entity.CategoryTranslation;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface CategoryMapper {

    @Mapping(target = "parentId", expression = "java(category.getParent() != null ? category.getParent().getId() : null)")
    @Mapping(target = "translations", source = "categoryTranslations", qualifiedByName = "mapTranslations")
    CategoryResponse toResponse(Category category);

    @Mapping(target = "parentId", expression = "java(category.getParent() != null ? category.getParent().getId() : null)")
    @Mapping(target = "parentName", expression = "java(category.getParent() != null ? category.getParent().getName() : null)")
    @Mapping(target = "translations", source = "categoryTranslations", qualifiedByName = "mapTranslations")
    CategoryInternalResponse toInternalResponse(Category category);

    TranslationResponse toTranslationResponse(CategoryTranslation translation);

    @Named("mapTranslations")
    default List<TranslationResponse> mapTranslations(Set<CategoryTranslation> translations) {
        if (translations == null) return List.of();
        return translations.stream()
                .map(this::toTranslationResponse)
                .collect(Collectors.toList());
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "categoryTranslations", ignore = true)
    @Mapping(target = "productCategories", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateCategory(UpdateCategoryRequest request, @MappingTarget Category category);
}
