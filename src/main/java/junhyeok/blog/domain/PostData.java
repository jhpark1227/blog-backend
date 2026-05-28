package junhyeok.blog.domain;

import java.time.LocalDateTime;

public record PostData(
        String pageId,
        String title,
        LocalDateTime createdTime,
        LocalDateTime lastEditedTime
) {
}