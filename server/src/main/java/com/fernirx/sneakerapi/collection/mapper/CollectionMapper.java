package com.fernirx.sneakerapi.collection.mapper;

import com.fernirx.sneakerapi.collection.dto.request.UpdateCollectionRequest;
import com.fernirx.sneakerapi.collection.dto.response.CollectionInternalResponse;
import com.fernirx.sneakerapi.collection.dto.response.CollectionResponse;
import com.fernirx.sneakerapi.collection.entity.Collection;
import org.mapstruct.*;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface CollectionMapper {

    CollectionResponse toResponse(Collection collection);

    @Mapping(target = "productCount", expression = "java(collection.getProductCollections().size())")
    CollectionInternalResponse toInternalResponse(Collection collection);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "productCollections", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateCollection(UpdateCollectionRequest request, @MappingTarget Collection collection);
}
