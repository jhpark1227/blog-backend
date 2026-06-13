package junhyeok.blog.domain;

import java.util.List;

public record PostDataResult(
        List<PostData> items,
        String nextCursor,
        boolean hasMore
) {
}