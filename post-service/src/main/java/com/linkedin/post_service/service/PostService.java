package com.linkedin.post_service.service;

import com.linkedin.post_service.entity.Comment;
import com.linkedin.post_service.entity.Like;
import com.linkedin.post_service.entity.Post;
import com.linkedin.post_service.repository.CommentRepository;
import com.linkedin.post_service.repository.LikeRepository;
import com.linkedin.post_service.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final S3Service s3Service;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private  static final String POST_CREATED_TOPIC = "post.created";
    private  static final String POST_LIKED_TOPIC = "post.liked";
    private  static final String POST_COMMENTED_TOPIC = "post.commented";



    /**
     * Create post
     * Optionally upload image to S3
     * Publish post.created event to kafka
     * Feed and search will consum them
     * @param authorId
     * @param content
     * @param image
     * @return
     */
    public Post createPost(String authorId, String content, MultipartFile image) {
        log.info("Creating post with authorId {}", authorId);

        Post post = new Post();
        post.setAuthorId(authorId);
        post.setContent(content);

        if (image != null && !image.isEmpty()){
            String imageUrl = s3Service.uploadFile(
                    image, "posts/",authorId
            );
            post.setImageUrl(imageUrl);
        }

        Post savedPost = postRepository.save(post);
        log.info("Saved post {}", savedPost.getId());

        // Publish to kafka so feed and search consumme
        Map<String,Object> postCreatedEvent = new HashMap<>();
        postCreatedEvent.put("postId",savedPost.getId());
        postCreatedEvent.put("postAuthorId",savedPost.getAuthorId());
        postCreatedEvent.put("postContent",savedPost.getContent());
        postCreatedEvent.put("postImageUrl",savedPost.getImageUrl());
        postCreatedEvent.put("createAt",savedPost.getCreatedAt().toString());
        kafkaTemplate.send(POST_CREATED_TOPIC,savedPost.getId(),postCreatedEvent);

        log.info("post.created event publish {}",  savedPost.getId());
        return savedPost;
    }

    public Post getPost(String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("post not found"));
    }

    public List<Post> getUserPosts(String userId) {
        return  postRepository
                .findByAuthorIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Like or unlike a post
     * @param postId
     * @param userId
     * @return
     */
    public String likePost(String postId, String userId) {
        Post post = getPost(postId);

        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {

            // unlike
            likeRepository.findByPostIdAndUserId(postId, userId)
                    .ifPresent(likeRepository::delete);
            post.setLikeCount(post.getLikeCount() - 1);
            postRepository.save(post);
            return "Post unliked";
        }

        // Like
        Like like = new Like();
        like.setPostId(postId);
        like.setUserId(userId);
        likeRepository.save(like);

        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);

        // Publish to kafka

        Map<String, Object> likeCreatedEvent = new HashMap<>();
        likeCreatedEvent.put("postId",postId);
        likeCreatedEvent.put("userId",userId);
        likeCreatedEvent.put("postAuthorId",post.getAuthorId());
        likeCreatedEvent.put("createAt",post.getCreatedAt().toString());

        kafkaTemplate.send(POST_LIKED_TOPIC,postId,likeCreatedEvent);

        return "Post liked";

    }

    /**
     * Add comment to post
     * @param postId
     * @param authorId
     * @param content
     * @return
     */
    public Comment addComment(String postId, String authorId, String content) {
        Post post = getPost(postId);

        Comment comment = new Comment();
        comment.setPostId(postId));
        comment.setAuthorId(authorId);
        comment.setContent(content);
        Comment savedComment = commentRepository.save(comment);

        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        // Post commented event
        Map<String, Object> commentCreatedEvent = new HashMap<>();
        commentCreatedEvent.put("postId",postId);
        commentCreatedEvent.put("authorId",authorId);
        commentCreatedEvent.put("commentId",savedComment.getId());
        commentCreatedEvent.put("postAuthorId",savedComment.getPostId());

        kafkaTemplate.send(POST_COMMENTED_TOPIC,postId,commentCreatedEvent);

        return savedComment;
    }

    /**
     * Get comments for a post
     * @param postId
     * @return
     */
    public List<Comment> getComments(String postId) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
    }


    public void deletePost(String postId, String userId) {
        Post post = getPost(postId);

        if (!post.getAuthorId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this post");
        }

        postRepository.delete(post);
        log.info("Deleted post {}", post.getId());

    }







}
