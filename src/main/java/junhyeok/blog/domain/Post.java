package junhyeok.blog.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    private String notionPageId;

    private String title;

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    private String content;

    private String excerpt;

    private LocalDateTime excerptGeneratedAt;

    private LocalDate publishedDate;

    private LocalDateTime notionLastEditedTime;

    private LocalDateTime syncedAt;

    boolean pinned;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToMany
    @JoinTable(name = "post_tag", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags = new ArrayList<>();

    public Post(String notionPageId, String title, PostStatus status, String content, LocalDate publishedDate,
                LocalDateTime notionLastEditedTime, Category category, List<Tag> tags, boolean pinned) {
        this.notionPageId = notionPageId;
        this.title = title;
        this.status = status;
        this.content = content;
        this.publishedDate = publishedDate;
        this.notionLastEditedTime = notionLastEditedTime;
        this.syncedAt = LocalDateTime.now();
        this.pinned = pinned;
        this.category = category;
        this.tags = tags;
    }

    public void update(String title, PostStatus status, String content, LocalDate publishedDate, LocalDateTime notionLastEditedTime,
                       Category category, List<Tag> tags, boolean pinned) {
        this.title = title;
        this.status = status;
        this.content = content;
        this.publishedDate = publishedDate;
        this.notionLastEditedTime = notionLastEditedTime;
        this.pinned = pinned;
        this.category = category;
        this.tags = tags;
    }

    public void sync() {
        this.syncedAt = LocalDateTime.now();
    }
}
