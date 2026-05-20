package com.project.revhive.social.repository;

import com.project.revhive.social.model.Follow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    List<Follow> findByFollowingId(Long followingId);

    List<Follow> findByFollowerId(Long followerId);

    long countByFollowingId(Long followingId);

    long countByFollowerId(Long followerId);

    List<Follow> findByFollowingId(Long followingId, Pageable pageable);

    List<Follow> findByFollowerId(Long followerId, Pageable pageable);

    @Query("SELECT f.followerId FROM Follow f WHERE f.followingId = :userId")
    List<Long> findFollowerIdsByFollowingId(@Param("userId") Long userId);

    @Query("SELECT f.followingId FROM Follow f WHERE f.followerId = :userId")
    List<Long> findFollowingIdsByFollowerId(@Param("userId") Long userId);

    @Query("SELECT COUNT(f) > 0 FROM Follow f WHERE f.followerId = :followerId AND f.followingId = :followingId")
    boolean isFollowing(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    // Get mutual followers between two users
    @Query("SELECT f1.followerId FROM Follow f1 JOIN Follow f2 ON f1.followerId = f2.followerId " +
            "WHERE f1.followingId = :userId1 AND f2.followingId = :userId2")
    List<Long> findMutualFollowers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    // Get recent follows
    List<Follow> findByFollowingIdOrderByCreatedAtDesc(Long followingId, Pageable pageable);
}
