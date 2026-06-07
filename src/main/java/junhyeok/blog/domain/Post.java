package junhyeok.blog.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SoftDelete;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    private String notionPageId;

    private String title;

    private LocalDateTime notionCreatedTime;

    private LocalDateTime notionLastEditedTime;

    @SoftDelete
    private boolean deleted;

    private LocalDateTime syncedAt;

    @ManyToMany
    @JoinTable(name = "post_tag",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags = new ArrayList<>();

    public Post(String notionPageId, String title, LocalDateTime notionCreatedTime, LocalDateTime notionLastEditedTime, List<Tag> tags) {
        this.notionPageId = notionPageId;
        this.title = title;
        this.notionCreatedTime = notionCreatedTime;
        this.notionLastEditedTime = notionLastEditedTime;
        this.syncedAt = LocalDateTime.now();
        setTags(tags);
    }

    public void update(String title, LocalDateTime notionLastEditedTime, List<Tag> tags) {
        this.title = title;
        this.notionLastEditedTime = notionLastEditedTime;
        setTags(tags);
    }

    private void setTags(List<Tag> newTags) {
        this.tags.forEach(tag -> tag.removePost(this));
        this.tags = newTags;
        newTags.forEach(tag -> tag.addPost(this));
    }

    public void restore() {
        this.deleted = false;
    }

    public void sync() {
        this.syncedAt = LocalDateTime.now();
    }
}
