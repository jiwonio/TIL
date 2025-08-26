package com.example.til.dto;

import com.example.til.domain.PostType;
import com.example.til.domain.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PostRequest {
    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    private String content;

    private PostType type = PostType.NORMAL;
    private Visibility visibility = Visibility.PUBLIC;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public PostType getType() { return type; }
    public void setType(PostType type) { this.type = type; }
    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }
}
