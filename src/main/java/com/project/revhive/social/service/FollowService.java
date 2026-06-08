package com.project.revhive.social.service;

import com.project.revhive.social.client.UserClient;
import com.project.revhive.social.dto.UserSummary;
import com.project.revhive.social.model.Follow;
import com.project.revhive.social.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import com.project.revhive.social.service.integration.NotificationIntegrationService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FollowService {

    private  final UserClient userClient;
    private final FollowRepository followRepository;
    private final NotificationIntegrationService notificationIntegrationService;

    // Follow a user
    @Transactional
    public Follow followUser(Long followerId, Long followingId) {
        // Cannot follow self
        if (followerId.equals(followingId)) {
            throw new RuntimeException("Cannot follow yourself");
        }

        // Check if already following
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new RuntimeException("Already following this user");
        }

        // Create follow relationship
        Follow follow = Follow.builder()
                .followerId(followerId)
                .followingId(followingId)
                .build();

        Follow savedFollow = followRepository.save(follow);

        log.info("User {} started following user {}", followerId, followingId);

        // Trigger real-time notification
        try {
            notificationIntegrationService.sendFollowNotification(followerId, followingId);
        } catch (Exception e) {
            log.error("Failed to trigger follow notification: {}", e.getMessage());
        }

        return savedFollow;
    }

    // Unfollow a user
    @Transactional
    public void unfollowUser(Long followerId, Long followingId) {
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new RuntimeException("Follow relationship not found"));

        followRepository.delete(follow);

        log.info("User {} unfollowed user {}", followerId, followingId);
    }

    // Check if user is following another user
    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.isFollowing(followerId, followingId);
    }

    // Get all followers of a user
    @Transactional(readOnly = true)
    public List<UserSummary> getFollowers(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return followRepository.findByFollowingId(userId, pageable)
                .stream()
                .map(Follow::getFollowerId)
                .map(userClient::getUser)
                .toList();
    }

    // Get all users (IDs) that a user is following
    @Transactional(readOnly = true)
    public List<UserSummary> getFollowing(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return followRepository.findByFollowerId(userId, pageable)
                .stream()
                .map(Follow::getFollowingId)
                .map(userClient::getUser)
                .toList();
    }

    // Get followers count
    @Transactional(readOnly = true)
    public long getFollowersCount(Long userId) {
        return followRepository.countByFollowingId(userId);
    }

    // Get following count
    @Transactional(readOnly = true)
    public long getFollowingCount(Long userId) {
        return followRepository.countByFollowerId(userId);
    }

    // Get follower IDs
    @Transactional(readOnly = true)
    public List<Long> getFollowerIds(Long userId) {
        return followRepository.findFollowerIdsByFollowingId(userId);
    }

    // Get following IDs
    @Transactional(readOnly = true)
    public List<Long> getFollowingIds(Long userId) {
        return followRepository.findFollowingIdsByFollowerId(userId);
    }

    // Get mutual followers (IDs)
    @Transactional(readOnly = true)
    public List<Long> getMutualFollowers(Long userId1, Long userId2) {
        return followRepository.findMutualFollowers(userId1, userId2);
    }

    // Get recent follows
    @Transactional(readOnly = true)
    public List<Follow> getRecentFollows(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return followRepository.findByFollowingIdOrderByCreatedAtDesc(userId, pageable);
    }

    // Bulk unfollow
    @Transactional
    public void bulkUnfollow(Long followerId, List<Long> followingIds) {
        for (Long followingId : followingIds) {
            try {
                unfollowUser(followerId, followingId);
            } catch (Exception e) {
                log.error("Error unfollowing user {}: {}", followingId, e.getMessage());
            }
        }
    }
}
