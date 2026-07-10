package com.fernirx.sneakerapi.notification.mapper;

import com.fernirx.sneakerapi.notification.dto.response.NotificationInternalResponse;
import com.fernirx.sneakerapi.notification.dto.response.NotificationResponse;
import com.fernirx.sneakerapi.notification.entity.Notification;
import com.fernirx.sneakerapi.notification.entity.NotificationRecipient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface NotificationMapper {

    @Mapping(target = "id", source = "notification.id")
    @Mapping(target = "type", source = "notification.type")
    @Mapping(target = "title", source = "notification.title")
    @Mapping(target = "message", source = "notification.message")
    @Mapping(target = "imagePublicId", source = "notification.imagePublicId")
    @Mapping(target = "link", source = "notification.link")
    @Mapping(target = "read", expression = "java(recipient.getReadAt() != null)")
    @Mapping(target = "createdAt", source = "notification.createdAt")
    NotificationResponse toResponse(NotificationRecipient recipient);

    @Mapping(target = "read", constant = "false")
    NotificationResponse toPushPayload(Notification notification);

    NotificationInternalResponse toInternalResponse(Notification notification);
}
