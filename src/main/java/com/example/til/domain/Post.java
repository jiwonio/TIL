package com.example.til.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_posts_author_id", columnList = "author_id"),
        @Index(name = "idx_posts_deleted", columnList = "deleted"),
        @Index(name = "idx_posts_visibility", columnList = "visibility")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false, foreignKey = @ForeignKey(name = "fk_posts_author"))
    private User author;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostType type = PostType.NORMAL;
}
