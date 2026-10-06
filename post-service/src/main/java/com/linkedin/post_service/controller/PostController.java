package com.linkedin.post_service.controller;

import com.linkedin.post_service.entity.Comment;
import com.linkedin.post_service.entity.Post;
import com.linkedin.post_service.repository.PostRepository;
import com.linkedin.post_service.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@Slf4j
@RequiredArgsConstructor
public class PostController {
    private  final PostService postService;


    // Create post
    @PostMapping
    public ResponseEntity<Post> createPost(
            @RequestParam String authorId,
            @RequestParam String content,
            @RequestParam(required = true)MultipartFile image){

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.createPost(authorId, content,image));

    }


    @GetMapping("/postId")
    public ResponseEntity<Post> getPost(@PathVariable String postId) {
        return ResponseEntity.ok(postService.getPost(postId));
    }


    @GetMapping("/user/{userId)")
    private ResponseEntity<List<Post>> getUserPosts(@PathVariable String userId) {
        return ResponseEntity.ok(postService.getUserPosts(userId));
    }


    @PostMapping("/{postId}/like")
    public  ResponseEntity<String> likePost(@PathVariable String postId , @RequestParam String userId{
        return ResponseEntity.ok(postService.likePost(postId, userId));
    }


    @PostMapping("/{postId}/comments")
    public ResponseEntity<Comment> addComment(
            @PathVariable String postId,
            @RequestParam String authorId,
            @RequestParam String content
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.addComment(postId, authorId, content));
    }


    @PostMapping("/{postId}")
    public  ResponseEntity<String> deletePost(@PathVariable String postId, @RequestParam String userId) {
        postService.deletePost(postId, userId);

        return ResponseEntity.ok("Post deleted");
    }


    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable String postId) {
        return ResponseEntity.ok(postService.getComments(postId));
    }


}
