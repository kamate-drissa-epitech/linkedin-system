package com.linkedin.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class NotificationService {

    /**
     * Consume user created notification
     * @param payload
     */
    @KafkaListener(topics = "user.created")
    public void consumeUserCreated(@Payload Map<String, Object> payload) {
        log.info("🔥 user.created EVENT RECEIVED: {}", payload);

        try {
            String userId = (String) payload.get("userId");
            String firstName = (String) payload.get("firstName");

            senNotification(userId,
                    "Welcome to my platform",
                    String.format("Welcome %s Your account has been created" +
                            "Start with professional", firstName ));



        }catch (Exception e){
            log.error("Error in consuming user created {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "connection.requested")
    public void consumeConnectionRequest(@Payload Map<String, Object> payload) {
        try {
            String receiverId = (String) payload.get("receiverId");
            String requestId = (String) payload.get("requestId");

            senNotification(
                    requestId,
                    "New connection request",
                    String.format(
                            "User %s want to connect with you",
                            receiverId
                    )
                    );

        } catch (Exception e) {
            log.error("Error in consuming connection request {}", e.getMessage());
        }
    }

    /**
     * Consume connection accepted
     * And notify user that connection has accepted
     * @param payload
     */
    @KafkaListener(topics = "connection.accepted")
    public void consumeConnectionAccepted(@Payload Map<String, Object> payload) {
        try {
            String receiverId = (String) payload.get("receiverId");
            String requestId = (String) payload.get("requestId");

            senNotification(
                    requestId,
                    "connection Accepted",
                    String.format(
                            "User %s accepted the connection request" +
                            "You are now connected",
                            requestId
                    )
            );
        }catch (Exception e){
            log.error("Error in consuming connection accepted {}", e.getMessage());
        }
    }


    /**
     * Consume post like event
     * @param payload
     */
    @KafkaListener(topics = "post.liked")
    public void consumePostLiked(@Payload Map<String, Object> payload) {
        try {
            String authorId = (String) payload.get("authorId");
            String userId = (String) payload.get("userId");
            String postId = (String) payload.get("postId");

            senNotification(authorId,
                    "Someone like your post",
                    String.format("User %s like your post %s", userId, postId)
                    );

        }catch (Exception e){
            log.error("Error in consuming post liked {}", e.getMessage());
        }
    }

    /**
     * Consume post comment event
     * @param payload
     */
    @KafkaListener(topics = "post.commented")
    public void consumePostCommented(@Payload Map<String, Object> payload) {
        try {
            String commenterId = (String) payload.get("authorId");
            String postId = (String) payload.get("postId");
            String postAuthorId = (String) payload.get("postAuthorId");

            senNotification(postAuthorId,
                    "Someone comment your post",
                    String.format("User %s comment your post %s", commenterId, postId)
            );

        }catch (Exception e){
            log.error("Error in consuming post commented {}", e.getMessage());
        }
    }

    private void senNotification(String userId, String title, String message) {
        log.info("-------------------------------------------------");
        log.info("NOTIFICATION SEND");
        log.info("To user {} ", userId);
        log.info("With title {} ", title);
        log.info("With message {} ", message);
        log.info("----------------------------------------------");

    }


}
