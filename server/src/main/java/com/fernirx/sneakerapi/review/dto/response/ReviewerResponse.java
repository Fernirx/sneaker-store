package com.fernirx.sneakerapi.review.dto.response;

import com.fernirx.sneakerapi.user.entity.User;
import com.fernirx.sneakerapi.user.entity.UserProfile;

public record ReviewerResponse(
        Long userId,
        String displayName,
        String avatarPublicId
) {
    public static ReviewerResponse from(User user) {
        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            return new ReviewerResponse(user.getId(), user.getEmail(), null);
        }
        String displayName = profile.getLastName() != null
                ? profile.getFirstName() + " " + profile.getLastName()
                : profile.getFirstName();
        return new ReviewerResponse(user.getId(), displayName, profile.getAvatarPublicId());
    }
}
