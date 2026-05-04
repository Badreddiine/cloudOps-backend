package io.cloudops.notificationservice.mapper;


import io.cloudops.notificationservice.dto.NotificationDTO;
import io.cloudops.notificationservice.entity.Notification;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationMapper {

    public NotificationDTO toDTO(Notification notification) {
        if (notification == null) return null;
        return NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .message(notification.getMessage())
                .incidentId(notification.getIncidentId())
                .read(notification.getRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public List<NotificationDTO> toDTOList(List<Notification> notifications) {
        return notifications.stream()
                .map(this::toDTO)
                .toList();
    }

    public Notification toEntity(NotificationDTO dto) {
        if (dto == null) return null;
        return Notification.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .type(dto.getType())
                .message(dto.getMessage())
                .incidentId(dto.getIncidentId())
                .read(dto.getRead() != null ? dto.getRead() : false)
                .build();
    }
}