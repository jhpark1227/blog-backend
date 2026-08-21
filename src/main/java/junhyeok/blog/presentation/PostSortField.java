package junhyeok.blog.presentation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostSortField {

    PUBLISHED_DATE("publishedDate"),
    ;

    private final String property;
}
