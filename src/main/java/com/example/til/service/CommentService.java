package com.example.til.service;

import com.example.til.domain.Comment;
import com.example.til.domain.Post;
import com.example.til.domain.User;
import com.example.til.repository.CommentRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;

    public CommentService(CommentRepository commentRepository, UserService userService) {
        this.commentRepository = commentRepository;
        this.userService = userService;
    }

    public List<Comment> findByPost(Post post) {
        return commentRepository.findByPostAndDeletedFalse(post);
    }

    public Optional<Comment> findActiveById(Long id) {
        return commentRepository.findByIdAndDeletedFalse(id);
    }

    @Transactional
    public Comment create(User author, Post post, String content, Comment parent) {
        if (!userService.isEligibleToPostOrComment(author)) {
            throw new IllegalStateException("이메일 인증 후 가입 1일 이후에만 작성할 수 있습니다.");
        }
        int depth = 0;
        if (parent != null) {
            if (parent.isDeleted()) {
                throw new IllegalArgumentException("삭제된 댓글에는 답글을 달 수 없습니다.");
            }
            depth = parent.getDepth() + 1;
            if (depth > 2) {
                throw new IllegalArgumentException("대댓글은 최대 2 depth 까지만 허용됩니다.");
            }
        }
        String safeContent = org.springframework.web.util.HtmlUtils.htmlEscape(content);
        Comment comment = Comment.builder()
            .post(post)
            .author(author)
            .parent(parent)
            .depth(depth)
            .content(safeContent)
            .build();
        return commentRepository.save(comment);
    }

    @Transactional
    public Comment update(Comment comment, User editor, String content) {
        checkCanModify(comment, editor);
        String safeContent = org.springframework.web.util.HtmlUtils.htmlEscape(content);
        comment.setContent(safeContent);
        return comment;
    }

    @Transactional
    public void softDelete(Comment comment, User requester) {
        checkCanModify(comment, requester);
        comment.setDeleted(true);
        comment.setDeletedAt(LocalDateTime.now());
        comment.setDeleteScheduledAt(LocalDateTime.now().plusHours(24));
    }

    public void checkCanModify(Comment comment, User user) {
        if (comment == null || user == null) throw new SecurityException("권한이 없습니다.");
        if (!(user.isAdmin() || comment.getAuthor().getId().equals(user.getId()))) {
            throw new SecurityException("권한이 없습니다.");
        }
    }
}
