package com.linkedin.usersservice.repository;

import com.linkedin.usersservice.entity.Connection;
import com.linkedin.usersservice.entity.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConnectionRepository extends JpaRepository<Connection, String> {
    Boolean existsByRequesterIdAndReceiverId(String requesterId, String receiverId);
    List<Connection> findByRequesterIdAndStatus(String requesterId, ConnectionStatus status);
}
