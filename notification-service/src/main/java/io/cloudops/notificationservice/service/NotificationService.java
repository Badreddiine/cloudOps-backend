package io.cloudops.notificationservice.service;

import io.cloudops.notificationservice.dto.NotificationDTO;
import io.cloudops.notificationservice.entity.Notification;
import io.cloudops.notificationservice.mapper.NotificationMapper;
import io.cloudops.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    // ─── WebSocket ────────────────────────────────────────────────────────────

    public NotificationDTO sendWebSocket(String userId, String type,
                                         String message, Long incidentId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .message(message)
                .incidentId(incidentId)
                .read(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        NotificationDTO dto = notificationMapper.toDTO(saved);

        messagingTemplate.convertAndSend("/topic/notifications/" + userId, dto);
        log.info("[WS] Notification sent to userId={} type={}", userId, type);

        return dto;
    }

    // ─── Email ────────────────────────────────────────────────────────────────

    public void sendEmail(String to, String subject,
                          String templateName, Context ctx) {
        try {
            String html = templateEngine.process(templateName, ctx);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("[EMAIL] Sent to={} subject={}", to, subject);
        } catch (Exception e) {
            log.error("[EMAIL] Failed to send to={} : {}", to, e.getMessage());
        }
    }

    // ─── Queries ──────────────────────────────────────────────────────────────

    public List<NotificationDTO> getAllByUserId(String userId) {
        return notificationMapper.toDTOList(
                notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
        );
    }

    public List<NotificationDTO> getUnreadByUserId(String userId) {
        return notificationMapper.toDTOList(
                notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
        );
    }

    public long countUnread(String userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsReadByUserId(userId);
        log.info("[NOTIF] All notifications marked as read for userId={}", userId);
    }

    public void markAsRead(Long id, String userId) {
        notificationRepository.markAsReadByIdAndUserId(id, userId);
        log.info("[NOTIF] Notification id={} marked as read for userId={}", id, userId);
    }
}