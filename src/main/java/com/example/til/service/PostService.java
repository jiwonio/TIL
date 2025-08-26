package com.example.til.service;

import com.example.til.domain.Post;
import com.example.til.domain.PostType;
import com.example.til.domain.User;
import com.example.til.domain.Visibility;
import com.example.til.repository.PostRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;

    public PostService(PostRepository postRepository, UserService userService) {
        this.postRepository = postRepository;
        this.userService = userService;
    }

    public List<Post> listPublicPosts() {
        return postRepository.findByDeletedFalseAndVisibility(Visibility.PUBLIC);
    }

    public Optional<Post> findActiveById(Long id) {
        return postRepository.findByIdAndDeletedFalse(id);
    }

    @Transactional
    public Post create(User author, String title, String content, PostType type, Visibility visibility) {
        if (!userService.isEligibleToPostOrComment(author)) {
            throw new IllegalStateException("이메일 인증 후 가입 1일 이후에만 작성할 수 있습니다.");
        }
        if (type == PostType.NOTICE && !author.isAdmin()) {
            throw new SecurityException("공지사항은 관리자만 작성할 수 있습니다.");
        }
        if (visibility == Visibility.PRIVATE && !author.isAdmin()) {
            throw new SecurityException("비공개 설정은 관리자만 할 수 있습니다.");
        }
        String safeTitle = org.springframework.web.util.HtmlUtils.htmlEscape(title);
        String safeContent = org.springframework.web.util.HtmlUtils.htmlEscape(content);
        Post post = Post.builder()
            .author(author)
            .title(safeTitle)
            .content(safeContent)
            .type(type == null ? PostType.NORMAL : type)
            .build();
        post.setVisibility(visibility == null ? Visibility.PUBLIC : visibility);
        return postRepository.save(post);
    }

    @Transactional
    public Post update(Post post, User editor, String title, String content, PostType type, Visibility visibility) {
        checkCanModify(post, editor);
        if (type == PostType.NOTICE && !editor.isAdmin()) {
            throw new SecurityException("공지사항은 관리자만 작성할 수 있습니다.");
        }
        if (visibility == Visibility.PRIVATE && !editor.isAdmin()) {
            throw new SecurityException("비공개 설정은 관리자만 할 수 있습니다.");
        }
        String safeTitle = org.springframework.web.util.HtmlUtils.htmlEscape(title);
        String safeContent = org.springframework.web.util.HtmlUtils.htmlEscape(content);
        post.setTitle(safeTitle);
        post.setContent(safeContent);
        if (type != null) post.setType(type);
        if (visibility != null) post.setVisibility(visibility);
        return post;
    }

    @Transactional
    public void softDelete(Post post, User requester) {
        checkCanModify(post, requester);
        post.setDeleted(true);
        post.setDeletedAt(LocalDateTime.now());
        post.setDeleteScheduledAt(LocalDateTime.now().plusHours(24));
    }

    public void checkCanModify(Post post, User user) {
        if (post == null || user == null) throw new SecurityException("권한이 없습니다.");
        if (!(user.isAdmin() || post.getAuthor().getId().equals(user.getId()))) {
            throw new SecurityException("권한이 없습니다.");
        }
    }
}
