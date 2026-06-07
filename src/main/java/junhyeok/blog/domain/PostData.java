package junhyeok.blog.domain;

import java.time.LocalDateTime;
import java.util.List;

public record PostData(
        String pageId,
        String title,
        LocalDateTime createdTime,
        LocalDateTime lastEditedTime,
        List<TagData> tagData
) {
}