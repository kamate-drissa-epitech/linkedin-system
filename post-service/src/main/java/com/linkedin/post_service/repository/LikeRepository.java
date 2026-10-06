package com.linkedin.post_service.repository;

import com.linkedin.post_service.entity.Like;
import com.linkedin.post_service.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, String> {
    boolean existsByPostIdAndUserId(String postId,  String userId);
    Optional<Like> findByPostIdAndUserId(String postId,String userId);
}
