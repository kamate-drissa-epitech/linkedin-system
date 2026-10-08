package com.linkedin.usersservice.service;


import com.linkedin.usersservice.dto.AuthResponse;
import com.linkedin.usersservice.dto.LoginRequest;
import com.linkedin.usersservice.dto.RegisterRequest;
import com.linkedin.usersservice.entity.User;
import com.linkedin.usersservice.entity.UserRole;
import com.linkedin.usersservice.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private  final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private  static final String USER_CREATED_TOPIC = "user.created";

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long  jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    public AuthResponse register(RegisterRequest registerRequest) {
        log.info("Received request to register user {}", registerRequest);

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("User with email already exists" + registerRequest.getEmail());
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setHeadline(registerRequest.getHeadline());
        user.setLocation(registerRequest.getLocation());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(UserRole.NORMAL_USER);
        User userSaved = userRepository.save(user);

        log.info("Saved user {}", userSaved.getId());

        // Publish user.created event
        // Search service consume this and indexes user

        Map<String,Object> userCreatedEvent = new HashMap<>();
        userCreatedEvent.put("userId", userSaved.getId());
        userCreatedEvent.put("firstName", userSaved.getFirstName());
        userCreatedEvent.put("lastName", userSaved.getLastName());
        userCreatedEvent.put("email", userSaved.getEmail());
        userCreatedEvent.put("headline", userSaved.getHeadline());
        userCreatedEvent.put("location", userSaved.getLocation());

        kafkaTemplate.send(
                USER_CREATED_TOPIC,
                userSaved.getId(),
                userCreatedEvent
        ).whenComplete((result, ex) -> {

            if (ex != null) {
                log.error("❌ Error publishing user.created", ex);
                return;
            }

            log.info(
                    "✅ user.created published: topic={}, partition={}, offset={}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
            );
        });
//        kafkaTemplate.send(USER_CREATED_TOPIC,userSaved.getId(), userCreatedEvent);

        log.info("user.created event published  {}", userSaved.getId());

        String token = generateToken(userSaved.getId(), userSaved.getEmail());

        return buildAuthResponse(userSaved, token);

    }


    public AuthResponse login(LoginRequest loginRequest) {
        log.info("Received request to login user {}", loginRequest);
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + loginRequest.getEmail()));

        // Bcrypt verify  - compare raw password to store hash
        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        log.info("Login user {}", user.getId());

        //Generate jwt token
        String token = generateToken(user.getId(), user.getEmail());

        return buildAuthResponse(user, token);

    }

    /**
     * Generate access token
     * @param userId
     * @param email
     * @return
     */
    private  String generateToken(String userId, String email) {
        return Jwts.builder()
                .claim("userId", userId)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigninKey(),  SignatureAlgorithm.HS256)
                .compact();

    }

    private Key  getSigninKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    /**
     * Generate new token if one expire
     * @param userId
     * @return
     */
    private String generateRefreshToken(String userId) {
        return Jwts.builder()
                .claim("userId", userId)
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigninKey(),  SignatureAlgorithm.HS256)
                .compact();
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(token);
        authResponse.setRefreshToken(generateRefreshToken(user.getId()));
        authResponse.setUserId(user.getId());
        authResponse.setEmail(user.getEmail());
        authResponse.setFirstName(user.getFirstName());
        authResponse.setLastName(user.getLastName());

        return authResponse;

    }


}
