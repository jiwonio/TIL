package com.example.til.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comments_post_id", columnList = "post_id"),
        @Index(name = "idx_comments_author_id", columnList = "author_id"),
        @Index(name = "idx_comments_deleted", columnList = "deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, foreignKey = @ForeignKey(name = "fk_comments_post"))
    private Post post;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false, foreignKey = @ForeignKey(name = "fk_comments_author"))
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "fk_comments_parent"))
    private Comment parent;

    @Column(nullable = false)
    private int depth; // 0 for top-level, 1 for reply

    @Lob
    @Column(nullable = false)
    private String content;
}
