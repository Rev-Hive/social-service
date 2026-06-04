package com.project.revhive.social.service.integration;

import com.project.revhive.social.dto.NotificationRequest;
import com.project.revhive.social.dto.UserProfileDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Service
@Slf4j
public class NotificationIntegrationService {

    private final RestTemplate restTemplate;
    private final String userServiceUrl;
    private final String notificationServiceUrl;

    public NotificationIntegrationService(
            RestTemplate restTemplate,
            @Value("${app.services.user-service}") String userServiceUrl,
            @Value("${app.services.notification-service}") String notificationServiceUrl
    ) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
        this.notificationServiceUrl = notificationServiceUrl;
    }

    public void sendFollowNotification(Long followerId, Long followingId) {
        try {
            log.info("Sending follow notification. Follower: {}, Following: {}", followerId, followingId);

            // 1. Fetch follower's username from user-service
            String followerUsername = "Someone";
            try {
                String userUrl = userServiceUrl + "/api/users/" + followerId;
                HttpHeaders headers = new HttpHeaders();
                try {
                    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attributes != null) {
                        String authHeader = attributes.getRequest().getHeader("Authorization");
                        if (authHeader != null && !authHeader.isEmpty()) {
                            headers.set("Authorization", authHeader);
                        }
                    }
                } catch (Exception ex) {
                    log.warn("Could not propagate JWT token: {}", ex.getMessage());
                }

                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ResponseEntity<UserProfileDto> response = restTemplate.exchange(
                        userUrl,
                        HttpMethod.GET,
                        entity,
                        UserProfileDto.class
                );

                UserProfileDto profile = response.getBody();
                if (profile != null && profile.getUsername() != null) {
                    followerUsername = profile.getUsername();
                }
            } catch (Exception e) {
                log.warn("Failed to fetch follower profile from user-service (URL: {}), falling back. Error: {}", userServiceUrl, e.getMessage());
            }

            String messageText = followerUsername.equals("Someone") 
                    ? "Someone started following you!" 
                    : "@" + followerUsername + " started following you!";

            // 2. Build NotificationRequest
            NotificationRequest request = NotificationRequest.builder()
                    .userId(followingId)
                    .title("New Follower")
                    .message(messageText)
                    .type("FOLLOW")
                    .build();

            // 3. Post to notification-service
            String notifUrl = notificationServiceUrl + "/api/notifications";
            HttpHeaders notifHeaders = new HttpHeaders();
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    String authHeader = attributes.getRequest().getHeader("Authorization");
                    if (authHeader != null && !authHeader.isEmpty()) {
                        notifHeaders.set("Authorization", authHeader);
                    }
                }
            } catch (Exception ex) {
                log.warn("Could not propagate JWT token to notification-service: {}", ex.getMessage());
            }

            HttpEntity<NotificationRequest> notifEntity = new HttpEntity<>(request, notifHeaders);
            restTemplate.exchange(notifUrl, HttpMethod.POST, notifEntity, Object.class);
            log.info("Successfully sent follow notification to notification-service");

        } catch (Exception e) {
            log.error("Failed to send follow notification due to unexpected error: {}", e.getMessage(), e);
        }
    }
}
