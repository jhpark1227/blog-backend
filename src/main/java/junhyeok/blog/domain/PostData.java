package junhyeok.blog.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PostData(
        String pageId,
        String title,
        PostStatus status,
        LocalDate publishedDate,
        LocalDateTime createdTime,
        LocalDateTime lastEditedTime,
        String categoryId,
        List<String> tagIds,
        boolean pinned
) {
}