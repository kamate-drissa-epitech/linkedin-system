package com.linkedin.feedservice.controller;

import com.linkedin.feedservice.service.FeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feed")
@Slf4j
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    /**
     * Get paginated feeds for user
     * Return list of post ID
     * Client fetch full details from Post service
     * @param userId
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<String>> getFeed(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return  ResponseEntity.ok(feedService.getFeed(userId, page, size));
    }


    /**
     * Clear user feed cache - usefull for testing
     * @param userId
     * @return
     */
    @DeleteMapping("/{userId}/cache")
    public ResponseEntity<String> clearFeed(@PathVariable String userId) {
        feedService.clearFeed(userId);

        return   ResponseEntity.ok("Feed has been cleared");
    }


}
