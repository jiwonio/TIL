package com.example.til.repository;

import com.example.til.domain.Comment;
import com.example.til.domain.Post;
import com.example.til.domain.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostAndDeletedFalse(Post post);
    List<Comment> findByAuthorAndDeletedFalse(User author);
    Optional<Comment> findByIdAndDeletedFalse(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Comment c where c.deleted = true and c.deleteScheduledAt <= :now")
    int deleteAllSoftDeletedScheduledBefore(@Param("now") LocalDateTime now);
}