package com.example.til.web;

import com.example.til.domain.Comment;
import com.example.til.domain.Post;
import com.example.til.domain.User;
import com.example.til.dto.CommentRequest;
import com.example.til.repository.UserRepository;
import com.example.til.security.SecurityUtils;
import com.example.til.service.CommentService;
import com.example.til.service.PostService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class CommentController {

    private final CommentService commentService;
    private final PostService postService;
    private final UserRepository userRepository;

    public CommentController(CommentService commentService, PostService postService, UserRepository userRepository) {
        this.commentService = commentService;
        this.postService = postService;
        this.userRepository = userRepository;
    }

    @PostMapping("/posts/{postId}/comments")
    public String create(@PathVariable Long postId,
                         @Valid @ModelAttribute("commentRequest") CommentRequest request,
                         BindingResult bindingResult,
                         Model model) {
        Post post = postService.findActiveById(postId).orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        if (bindingResult.hasErrors()) {
            model.addAttribute("post", post);
            model.addAttribute("comments", commentService.findByPost(post));
            return "posts/detail";
        }
        User author = SecurityUtils.getCurrentUserEmail()
            .flatMap(userRepository::findByEmailIgnoreCase)
            .orElseThrow(() -> new SecurityException("로그인이 필요합니다."));
        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentService.findActiveById(request.getParentId()).orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));
        }
        commentService.create(author, post, request.getContent(), parent);
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/comments/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("commentRequest") CommentRequest request,
                         BindingResult bindingResult) {
        Comment comment = commentService.findActiveById(id).orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        if (bindingResult.hasErrors()) {
            return "redirect:/posts/" + comment.getPost().getId();
        }
        User editor = SecurityUtils.getCurrentUserEmail()
            .flatMap(userRepository::findByEmailIgnoreCase)
            .orElseThrow(() -> new SecurityException("로그인이 필요합니다."));
        commentService.update(comment, editor, request.getContent());
        return "redirect:/posts/" + comment.getPost().getId();
    }

    @PostMapping("/comments/{id}/delete")
    public String delete(@PathVariable Long id) {
        Comment comment = commentService.findActiveById(id).orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        User requester = SecurityUtils.getCurrentUserEmail()
            .flatMap(userRepository::findByEmailIgnoreCase)
            .orElseThrow(() -> new SecurityException("로그인이 필요합니다."));
        commentService.softDelete(comment, requester);
        return "redirect:/posts/" + comment.getPost().getId();
    }
}
