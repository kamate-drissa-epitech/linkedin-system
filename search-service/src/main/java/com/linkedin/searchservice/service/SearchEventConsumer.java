package com.linkedin.searchservice.service;

import com.linkedin.searchservice.model.PostDocument;
import com.linkedin.searchservice.model.UserDocument;
import com.linkedin.searchservice.repository.PostSearchRepository;
import com.linkedin.searchservice.repository.UserSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchEventConsumer {

    private final UserSearchRepository userSearchRepository;
    private final PostSearchRepository postSearchRepository;

    @KafkaListener(topics = "user.created")
    public void consumeUserCreated(
            @Payload Map<String, Object> payload
            ) {
        log.info("🔥 Search service received user.created: {}", payload);

        try {
            log.info("Indexing new user {}", payload.get("userId"));
            UserDocument userDocument = new UserDocument();
            userDocument.setId((String) payload.get("userId"));
            userDocument.setFirstName((String) payload.get("firstName"));
            userDocument.setLastName((String) payload.get("lastName"));
            userDocument.setEmail((String) payload.get("email"));
            userDocument.setHeadline((String) payload.get("headline"));
            userDocument.setLocation((String) payload.get("location"));

            userSearchRepository.save(userDocument);
            log.info("User indexed {}", userDocument.getId());

        }catch (Exception e){
            log.error("Error indexing {}", e.getMessage());
        }
    }


    @KafkaListener(topics = "user.updated")
    public void consumeUserUpdated(
            @Payload Map<String, Object> payload
    ){
        try {
            String userId = (String) payload.get("userId");
            log.info("Indexing user updated {}", payload.get("userId"));

            userSearchRepository.findById(userId).ifPresent(doc -> {
                doc.setFirstName((String) payload.get("firstName"));
                doc.setLastName((String) payload.get("lastName"));
                doc.setEmail((String) payload.get("email"));
                doc.setHeadline((String) payload.get("headline"));
                doc.setLocation((String) payload.get("location"));

                Object skills = payload.get("skills");
                if (skills instanceof List<?> list) {
                    List<String> stringSkills = list.stream()
                            .filter(String.class::isInstance)
                            .map(String.class::cast)
                            .toList();
                    doc.setSkills(stringSkills);
                }
                userSearchRepository.save(doc);
                log.info("User index  updated{}", doc.getId());
            });

        }catch (Exception e){
            log.error("Error user indexing update {}", e.getMessage());
        }
    }


    @KafkaListener(topics = "post.created")
    public void consumePostCreated(
            @Payload Map<String, Object> payload) {
        try {
            PostDocument  postDocument = new PostDocument();
            postDocument.setId((String) payload.get("postId"));
            postDocument.setContent((String) payload.get("content"));
            postDocument.setAuthorId((String) payload.get("authorId"));
            postDocument.setImageUrl((String) payload.get("imageUrl"));
            postDocument.setCreatedAt((LocalDateTime) payload.get("createdAt"));

            postSearchRepository.save(postDocument);

        } catch (Exception e) {
            log.error("Error post  indexing {}", e.getMessage());
        }
    }


}
