package junhyeok.blog.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SoftDelete;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String notionPageId;

    private String title;

    private LocalDateTime notionCreatedTime;

    private LocalDateTime notionLastEditedTime;

    @SoftDelete
    private boolean deleted;

    private LocalDateTime syncedAt;

    public Post(String notionPageId, String title, LocalDateTime notionCreatedTime, LocalDateTime notionLastEditedTime) {
        this.notionPageId = notionPageId;
        this.title = title;
        this.notionCreatedTime = notionCreatedTime;
        this.notionLastEditedTime = notionLastEditedTime;
        this.syncedAt = LocalDateTime.now();
    }

    public void update(String title, LocalDateTime notionLastEditedTime) {
        this.title = title;
        this.notionLastEditedTime = notionLastEditedTime;
    }

    public void restore() {
        this.deleted = false;
    }

    public void sync() {
        this.syncedAt = LocalDateTime.now();
    }
}
