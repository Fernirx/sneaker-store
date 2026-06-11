package com.fernirx.sneakerapi.user.mapper;

import com.fernirx.sneakerapi.user.dto.request.UpdateProfileRequest;
import com.fernirx.sneakerapi.user.dto.response.ProfileResponse;
import com.fernirx.sneakerapi.user.entity.User;
import com.fernirx.sneakerapi.user.entity.UserProfile;
import org.mapstruct.*;


@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface ProfileMapper {

    @Mapping(source = "userProfile.firstName", target = "firstName")
    @Mapping(source = "userProfile.lastName", target = "lastName")
    @Mapping(source = "userProfile.phone", target = "phone")
    @Mapping(source = "userProfile.dateOfBirth", target = "dateOfBirth")
    @Mapping(source = "userProfile.avatarPublicId", target = "avatarPublicId")
    @Mapping(target = "emailVerified", expression = "java(user.getVerifiedAt() != null)")
    ProfileResponse toProfileResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateUserProfile(UpdateProfileRequest request, @MappingTarget UserProfile userProfile);
}
