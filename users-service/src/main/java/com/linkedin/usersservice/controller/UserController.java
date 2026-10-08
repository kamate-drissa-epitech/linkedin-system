package com.linkedin.usersservice.controller;

import com.linkedin.usersservice.dto.UserResponse;
import com.linkedin.usersservice.entity.User;
import com.linkedin.usersservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Slf4j
public class UserController {

    private UserService userService;

    /***
     * Get User profile
     * @param userId
     * @param requestingUserId
     * @return
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable String userId,
                                                       @RequestHeader("X-User-Id") String requestingUserId) {
        log.info("Get profile: {} requested by: {}", userId, requestingUserId);

        return ResponseEntity.ok(userService.getUserProfile(userId));

    }

    /**
     * Udpate own profile
     * User can only update their own profile
     * @param userId
     * @param requestingUserId
     * @return
     */
    @PutMapping("/userId/profile")
    public ResponseEntity<UserResponse> updateUserProfile(@PathVariable String userId,@RequestHeader("X-User-Id") String requestingUserId, @RequestBody UserResponse request) {
        if (!userId.equals(requestingUserId)) {
            return  ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.updateUserProfile(userId, request));
    }

    @PostMapping("/{userId}/profile-photo")
    public ResponseEntity<UserResponse> uploadProfilePhoto(
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String requestingUserId,
            @RequestParam("file") MultipartFile file
            ){

            if (!userId.equals(requestingUserId)) {
                return ResponseEntity.status(403).build();
            }

            return  ResponseEntity.ok(userService.uploadProfilePhoto(userId, file));
    }

    /**
     * Send connection request to one user
     * @param targetedUserId
     * @param requestingUserId
     * @return
     */
    @PostMapping("/{targetedUserId}/connect")
    public ResponseEntity<String> sendConnection(@PathVariable String targetedUserId, @RequestHeader("X-User-Id") String requestingUserId) {
        return ResponseEntity.ok(userService.sendConnectionRequest(targetedUserId, requestingUserId));
    }


    @PutMapping("/connection/{connectionId}/accept")
    public ResponseEntity<String> acceptConnection(@PathVariable String connectionId, @RequestHeader("X-User-Id") String requestingUserId) {
        return  ResponseEntity.ok(userService.acceptConnectionRequest(connectionId));
    }

    @GetMapping("/{userId}/getConnections")
    public ResponseEntity<List<UserResponse>> getConnections(@PathVariable String userId, @RequestHeader("X-User-Id") String requestingUserId) {
        return  ResponseEntity.ok(userService.getConnections(userId));
    }
}
