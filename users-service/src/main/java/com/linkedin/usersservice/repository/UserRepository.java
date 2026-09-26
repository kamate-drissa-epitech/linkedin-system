package com.linkedin.usersservice.repository;

import com.linkedin.usersservice.entity.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}
