package com.fernirx.sneakerapi.collection.mapper;

import com.fernirx.sneakerapi.collection.dto.request.UpdateCollectionRequest;
import com.fernirx.sneakerapi.collection.dto.response.CollectionInternalResponse;
import com.fernirx.sneakerapi.collection.dto.response.CollectionResponse;
import com.fernirx.sneakerapi.collection.dto.response.TranslationResponse;
import com.fernirx.sneakerapi.collection.entity.Collection;
import com.fernirx.sneakerapi.collection.entity.CollectionTranslation;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface CollectionMapper {

    @Mapping(target = "translations", source = "collectionTranslations", qualifiedByName = "mapTranslations")
    CollectionResponse toResponse(Collection collection);

    @Mapping(target = "translations", source = "collectionTranslations", qualifiedByName = "mapTranslations")
    CollectionInternalResponse toInternalResponse(Collection collection);

    TranslationResponse toTranslationResponse(CollectionTranslation translation);

    @Named("mapTranslations")
    default List<TranslationResponse> mapTranslations(Set<CollectionTranslation> translations) {
        if (translations == null) return List.of();
        return translations.stream()
                .map(this::toTranslationResponse)
                .collect(Collectors.toList());
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "collectionTranslations", ignore = true)
    @Mapping(target = "productCollections", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateCollection(UpdateCollectionRequest request, @MappingTarget Collection collection);
}
