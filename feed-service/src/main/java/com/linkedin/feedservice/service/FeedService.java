package com.linkedin.feedservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String FEED_KEY_PREFIX = "feed:";
    private final FeedService feedService;

    /**
     *
     * @param userId
     * @param page
     * @param size
     * @return
     */
    public List<String> getFeed(String userId, int page, int size){
        log.info("Getting feed for use {} ", userId);

        String key = FEED_KEY_PREFIX + userId;

        //Pagination post Range

        int start = page * size;
        int end = start + size - 1;

        List<Object> postIds = redisTemplate.opsForList().range(key, start, end);

        if (postIds == null || postIds.isEmpty()){
            log.info("No feed cache for user {} ", userId);
            return new ArrayList<>();
        }

        List<String> result = postIds.stream()
                .map(Object::toString)
                .toList();
        log.info("Found {} feeds for user {} ", result.size(), userId);
        return result;
    }

    /**
     * Clear feed cache for user
     * @param userId
     */
    public void cleaFeed(String userId){
        String key = FEED_KEY_PREFIX + userId;
        redisTemplate.delete(key);
        log.info("Deleted feed cache for user {} ", userId);
    }

}
