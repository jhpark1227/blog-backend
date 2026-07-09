package junhyeok.blog.domain;

import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import junhyeok.blog.global.exception.CustomException;
import junhyeok.blog.global.exception.ErrorCode;

@Embeddable
public record Excerpt(
        String value,
        LocalDateTime generatedAt
) {
    public static final int MAX_LENGTH = 150;

    public Excerpt {
        if (value == null || value.isBlank() || value.length() > MAX_LENGTH) {
            throw new CustomException(ErrorCode.INVALID_EXCERPT_LENGTH);
        }
    }

    public Excerpt(String value) {
        this(value, LocalDateTime.now());
    }
}
