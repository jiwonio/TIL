package com.example.til.repository;

import com.example.til.domain.Post;
import com.example.til.domain.User;
import com.example.til.domain.Visibility;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByDeletedFalseAndVisibility(Visibility visibility);
    List<Post> findByAuthorAndDeletedFalse(User author);
    Optional<Post> findByIdAndDeletedFalse(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Post p where p.deleted = true and p.deleteScheduledAt <= :now")
    int deleteAllSoftDeletedScheduledBefore(@Param("now") LocalDateTime now);
}
