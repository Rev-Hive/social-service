package com.project.revhive.social.controller;

import com.project.revhive.social.dto.UserSummary;
import com.project.revhive.social.model.Follow;
import com.project.revhive.social.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/follows")
@RequiredArgsConstructor
@Slf4j
public class FollowController {

    private final FollowService followService;

    // Follow a user
    @PostMapping("/follow")
    public ResponseEntity<Map<String, Object>> followUser(
            @RequestParam Long followerId,
            @RequestParam Long followingId) {

        try {
            Follow follow = followService.followUser(followerId, followingId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully followed user");
            response.put("data", follow);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // Unfollow a user
    @DeleteMapping("/unfollow")
    public ResponseEntity<Map<String, Object>> unfollowUser(
            @RequestParam Long followerId,
            @RequestParam Long followingId) {

        try {
            followService.unfollowUser(followerId, followingId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully unfollowed user");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // Check if following
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> isFollowing(
            @RequestParam Long followerId,
            @RequestParam Long followingId) {

        boolean isFollowing = followService.isFollowing(followerId, followingId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("isFollowing", isFollowing);

        return ResponseEntity.ok(response);
    }

    // Get followers of a user
    @GetMapping("/users/{userId}/followers")
    public ResponseEntity<Map<String, Object>> getFollowers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<Long> followers = followService.getFollowers(userId, page, size);
        long totalCount = followService.getFollowersCount(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", followers);
        response.put("totalCount", totalCount);
        response.put("currentPage", page);
        response.put("pageSize", size);

        return ResponseEntity.ok(response);
    }

    // Get following of a user
    @GetMapping("/users/{userId}/following")
    public ResponseEntity<Map<String, Object>> getFollowing(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<UserSummary> following =
                followService.getFollowing(userId, page, size);
        long totalCount = followService.getFollowingCount(userId);
        Map<String, Object> response = new HashMap<>();
        response = new HashMap<>();
        response.put("success", true);
        response.put("data", following);
        response.put("totalCount", totalCount);
        response.put("currentPage", page);
        response.put("pageSize", size);

        return ResponseEntity.ok(response);
    }

    // Get followers count
    @GetMapping("/users/{userId}/followers/count")
    public ResponseEntity<Map<String, Object>> getFollowersCount(@PathVariable Long userId) {

        long count = followService.getFollowersCount(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", userId);
        response.put("followersCount", count);

        return ResponseEntity.ok(response);
    }

    // Get following count
    @GetMapping("/users/{userId}/following/count")
    public ResponseEntity<Map<String, Object>> getFollowingCount(@PathVariable Long userId) {

        long count = followService.getFollowingCount(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", userId);
        response.put("followingCount", count);

        return ResponseEntity.ok(response);
    }

    // Get follower IDs
    @GetMapping("/users/{userId}/follower-ids")
    public ResponseEntity<Map<String, Object>> getFollowerIds(@PathVariable Long userId) {

        List<Long> followerIds = followService.getFollowerIds(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", userId);
        response.put("followerIds", followerIds);
        response.put("count", followerIds.size());

        return ResponseEntity.ok(response);
    }

    // Get following IDs
    @GetMapping("/users/{userId}/following-ids")
    public ResponseEntity<Map<String, Object>> getFollowingIds(@PathVariable Long userId) {

        List<Long> followingIds = followService.getFollowingIds(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", userId);
        response.put("followingIds", followingIds);
        response.put("count", followingIds.size());

        return ResponseEntity.ok(response);
    }

    // Get mutual followers between two users
    @GetMapping("/mutual")
    public ResponseEntity<Map<String, Object>> getMutualFollowers(
            @RequestParam Long userId1,
            @RequestParam Long userId2) {

        List<Long> mutualFollowers = followService.getMutualFollowers(userId1, userId2);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId1", userId1);
        response.put("userId2", userId2);
        response.put("mutualFollowers", mutualFollowers);
        response.put("count", mutualFollowers.size());

        return ResponseEntity.ok(response);
    }

    // Get recent follows
    @GetMapping("/users/{userId}/recent")
    public ResponseEntity<Map<String, Object>> getRecentFollows(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {

        List<Follow> recentFollows = followService.getRecentFollows(userId, limit);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", userId);
        response.put("recentFollows", recentFollows);
        response.put("count", recentFollows.size());

        return ResponseEntity.ok(response);
    }

    // Bulk unfollow
    @DeleteMapping("/bulk-unfollow")
    public ResponseEntity<Map<String, Object>> bulkUnfollow(
            @RequestParam Long followerId,
            @RequestBody List<Long> followingIds) {

        followService.bulkUnfollow(followerId, followingIds);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Successfully unfollowed " + followingIds.size() + " users");

        return ResponseEntity.ok(response);
    }
}
