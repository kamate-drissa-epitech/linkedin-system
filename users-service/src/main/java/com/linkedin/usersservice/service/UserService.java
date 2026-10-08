package com.linkedin.usersservice.service;

import com.linkedin.usersservice.dto.UserResponse;
import com.linkedin.usersservice.entity.Connection;
import com.linkedin.usersservice.entity.ConnectionStatus;
import com.linkedin.usersservice.entity.User;
import com.linkedin.usersservice.repository.ConnectionRepository;
import com.linkedin.usersservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private  final ConnectionRepository connectionRepository;

    private  final UserRepository userRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final S3Service s3Service;

    private static final String CONNECTION_REQUESTED_TOPIC = "connection.requested";
    private static final String CONNECTION_ACCEPTED_TOPIC = "connection.accepted";
    private static final String USER_UPDATED_TOPIC = "user.updated";

    public String sendConnectionRequest(String receiverId, String requesterId){

        if (connectionRepository.existsByRequesterIdAndReceiverId(requesterId, receiverId)) {
            throw new RuntimeException("Connection request already exists");
        }

        Connection connection = new Connection();
        connection.setRequesterId(requesterId);
        connection.setReceiverId(receiverId);
        connection.setStatus(ConnectionStatus.PENDING);

        connectionRepository.save(connection);

        // Publish connection.requested event

        Map<String,Object> connectionRequestEvent = new HashMap<>();
        connectionRequestEvent.put("requesterId", requesterId);
        connectionRequestEvent.put("receiverId", receiverId);

        kafkaTemplate.send(CONNECTION_REQUESTED_TOPIC, connectionRequestEvent);
        log.info("Connection request sent {} -> {}", requesterId, receiverId);

        return "Connection request sent " + requesterId;
    }


    public String acceptConnectionRequest(String connectionId) {
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new RuntimeException("Connection request not found"));

        connection.setStatus(ConnectionStatus.CONNECTED);
        connectionRepository.save(connection);

        //Publish connetion.accepted event
        Map<String,Object> connectionAcceptedEvent = new HashMap<>();
        connectionAcceptedEvent.put("requesterId", connection.getRequesterId());
        connectionAcceptedEvent.put("receiverId", connection.getReceiverId());

        kafkaTemplate.send(CONNECTION_ACCEPTED_TOPIC, connectionAcceptedEvent);
        log.info("Connection accepted  {}", connectionId);

        return "Connection accepted " + connectionId;
    }


    public List<UserResponse> getConnections(String userId) {
       List<Connection> connections =  connectionRepository.findByRequesterIdAndStatus(userId, ConnectionStatus.CONNECTED);

       return connections.stream()
               .map((Connection c) -> getUserProfile(c.getReceiverId()))
               .collect(Collectors.toList());
    }


    public UserResponse getUserProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToResponse(user);

    }

    public UserResponse updateUserProfile(String userId, UserResponse request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setHeadline(request.getHeadline());
        user.setAbout(request.getAbout());
        user.setLocation(request.getLocation());
        user.setSkills(request.getSkills());

        User userSaved = userRepository.save(user);

        // Publish user.updated event

        Map<String,Object> userUpdatedEvent = new HashMap<>();
        userUpdatedEvent.put("userId", userSaved.getId());
        userUpdatedEvent.put("userName", userSaved.getFirstName());
        userUpdatedEvent.put("userLastName", userSaved.getLastName());
        userUpdatedEvent.put("userHeadline", userSaved.getHeadline());
        userUpdatedEvent.put("userLocation", userSaved.getLocation());
        userUpdatedEvent.put("userSkills", userSaved.getSkills());


        kafkaTemplate.send(USER_UPDATED_TOPIC,userSaved.getId(), userUpdatedEvent);

        log.info("user.updated event published  {}", userSaved.getId());

        return mapToResponse(userSaved);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setAbout(user.getAbout());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setCoverPhotoUrl(user.getCoverPhotoUrl());
        response.setHeadline(user.getHeadline());
        response.setLocation(user.getLocation());
        response.setProfilePhotoUrl(user.getProfilePhotoUrl());
        response.setSkills(user.getSkills());
        response.setRole(user.getRole());

        return response;
    }


    public UserResponse uploadProfilePhoto(String userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String photoUrl = s3Service.uploadFile(file, "/profile" + userId + "/avatar");

        user.setProfilePhotoUrl(photoUrl);

        User savedUser = userRepository.save(user);

        log.info("Profile photo uploaded for user {}", savedUser.getId());

        return mapToResponse(savedUser);
    }

}
