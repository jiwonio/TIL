package com.example.til.service;

import com.example.til.repository.CommentRepository;
import com.example.til.repository.PostRepository;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CleanupScheduler {
    private static final Logger log = LoggerFactory.getLogger(CleanupScheduler.class);

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public CleanupScheduler(PostRepository postRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void purgeSoftDeleted() {
        LocalDateTime now = LocalDateTime.now();
        int posts = postRepository.deleteAllSoftDeletedScheduledBefore(now);
        int comments = commentRepository.deleteAllSoftDeletedScheduledBefore(now);
        if (posts > 0 || comments > 0) {
            log.info("Purged soft-deleted: posts={}, comments={}", posts, comments);
        }
    }
}
