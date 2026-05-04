package io.cloudops.notificationservice.controller;

import io.cloudops.notificationservice.dto.NotificationDTO;
import io.cloudops.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // GET /api/notifications — toutes les notifications de l'utilisateur
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getAll(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                notificationService.getAllByUserId(jwt.getSubject())
        );
    }

    // GET /api/notifications/unread — non lues uniquement
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnread(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                notificationService.getUnreadByUserId(jwt.getSubject())
        );
    }

    // GET /api/notifications/unread/count — badge count
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> countUnread(@AuthenticationPrincipal Jwt jwt) {
        long count = notificationService.countUnread(jwt.getSubject());
        return ResponseEntity.ok(Map.of("count", count));
    }

    // PUT /api/notifications/read-all — marquer tout comme lu
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal Jwt jwt) {
        notificationService.markAllAsRead(jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    // PUT /api/notifications/{id}/read — marquer une notification comme lue
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id,
                                           @AuthenticationPrincipal Jwt jwt) {
        notificationService.markAsRead(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}
