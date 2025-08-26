package com.example.til.web;

import com.example.til.domain.Post;
import com.example.til.domain.PostType;
import com.example.til.domain.User;
import com.example.til.domain.Visibility;
import com.example.til.dto.PostRequest;
import com.example.til.repository.UserRepository;
import com.example.til.security.SecurityUtils;
import com.example.til.service.CommentService;
import com.example.til.service.PostService;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final UserRepository userRepository;

    public PostController(PostService postService, CommentService commentService, UserRepository userRepository) {
        this.postService = postService;
        this.commentService = commentService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("posts", postService.listPublicPosts());
        return "posts/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Post post = postService.findActiveById(id).orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        if (post.getVisibility() == Visibility.PRIVATE) {
            // only admin can view
            boolean admin = SecurityUtils.getCurrentUserEmail()
                .flatMap(userRepository::findByEmailIgnoreCase)
                .map(User::isAdmin)
                .orElse(false);
            if (!admin) throw new SecurityException("비공개 글입니다.");
        }
        model.addAttribute("post", post);
        model.addAttribute("comments", commentService.findByPost(post));
        model.addAttribute("commentRequest", new com.example.til.dto.CommentRequest());
        return "posts/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("postRequest", new PostRequest());
        model.addAttribute("types", PostType.values());
        model.addAttribute("visibilities", Visibility.values());
        return "posts/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("postRequest") PostRequest request,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("types", PostType.values());
            model.addAttribute("visibilities", Visibility.values());
            return "posts/form";
        }
        User author = SecurityUtils.getCurrentUserEmail()
            .flatMap(userRepository::findByEmailIgnoreCase)
            .orElseThrow(() -> new SecurityException("로그인이 필요합니다."));
        Post post = postService.create(author, request.getTitle(), request.getContent(), request.getType(), request.getVisibility());
        return "redirect:/posts/" + post.getId();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Post post = postService.findActiveById(id).orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        User editor = SecurityUtils.getCurrentUserEmail()
            .flatMap(userRepository::findByEmailIgnoreCase)
            .orElseThrow(() -> new SecurityException("로그인이 필요합니다."));
        postService.checkCanModify(post, editor);
        PostRequest pr = new PostRequest();
        pr.setTitle(post.getTitle());
        pr.setContent(post.getContent());
        pr.setType(post.getType());
        pr.setVisibility(post.getVisibility());
        model.addAttribute("post", post);
        model.addAttribute("postRequest", pr);
        model.addAttribute("types", PostType.values());
        model.addAttribute("visibilities", Visibility.values());
        return "posts/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("postRequest") PostRequest request,
                         BindingResult bindingResult,
                         Model model) {
        Post post = postService.findActiveById(id).orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        if (bindingResult.hasErrors()) {
            model.addAttribute("post", post);
            model.addAttribute("types", PostType.values());
            model.addAttribute("visibilities", Visibility.values());
            return "posts/form";
        }
        User editor = SecurityUtils.getCurrentUserEmail()
            .flatMap(userRepository::findByEmailIgnoreCase)
            .orElseThrow(() -> new SecurityException("로그인이 필요합니다."));
        postService.update(post, editor, request.getTitle(), request.getContent(), request.getType(), request.getVisibility());
        return "redirect:/posts/" + post.getId();
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        Post post = postService.findActiveById(id).orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        User requester = SecurityUtils.getCurrentUserEmail()
            .flatMap(userRepository::findByEmailIgnoreCase)
            .orElseThrow(() -> new SecurityException("로그인이 필요합니다."));
        postService.softDelete(post, requester);
        return "redirect:/";
    }
}
