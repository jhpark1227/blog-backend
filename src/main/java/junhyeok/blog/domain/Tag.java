package junhyeok.blog.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

    @Id
    String notionOptionId;

    String name;

    String color;

    int sortOrder;

    public Tag(String notionOptionId, String name, String color, int sortOrder) {
        this.notionOptionId = notionOptionId;
        this.name = name;
        this.color = color;
        this.sortOrder = sortOrder;
    }
}
